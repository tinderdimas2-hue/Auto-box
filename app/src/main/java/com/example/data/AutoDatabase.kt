package com.example.data

import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.OnConflictStrategy
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

// ----------------------------------------------------
// ENTITIES
// ----------------------------------------------------

@Entity(tableName = "establishments")
data class Establishment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // "OFICINA" (Workshop), "AUTO_PECAS" (Parts Store), "SUCATA" (Junkyard)
    val address: String,
    val phone: String, // WhatsApp
    val specialties: String, // Comma-separated specialties
    val rating: Float = 4.5f,
    val reviewsCount: Int = 12
) {
    fun typeLabel(): String = when (type) {
        "OFICINA" -> "Oficina Mecânica"
        "AUTO_PECAS" -> "Auto Peças"
        "SUCATA" -> "Sucata / Desmanche"
        else -> type
    }
}

@Entity(tableName = "offerings")
data class Offering(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val establishmentId: Int,
    val name: String,
    val type: String, // "PART" or "SERVICE"
    val price: Double,
    val condition: String, // "Nova", "Usada", "N/A" (for services)
    val description: String
)

@Entity(tableName = "quotation_requests")
data class QuotationRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clientName: String,
    val carModel: String,
    val itemDescription: String,
    val category: String, // "PART" or "SERVICE"
    val status: String = "OPEN", // "OPEN", "CLOSED"
    val timestamp: Long = System.currentTimeMillis(),
    val contactPhone: String
)

@Entity(tableName = "quotation_proposals")
data class QuotationProposal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val requestId: Int,
    val establishmentId: Int,
    val price: Double,
    val condition: String, // "Nova", "Usada", "Original Reciclada", "N/A"
    val notes: String,
    val estimatedDays: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)

// Helper model to represent proposal with shop details
data class RichProposal(
    val proposal: QuotationProposal,
    val shopName: String,
    val shopPhone: String,
    val shopType: String,
    val shopRating: Float
)

// ----------------------------------------------------
// DAO DEFINITION
// ----------------------------------------------------

@Dao
interface AutoDao {
    // Establishments
    @Query("SELECT * FROM establishments ORDER BY rating DESC")
    fun getAllEstablishmentsFlow(): Flow<List<Establishment>>

    @Query("SELECT * FROM establishments WHERE id = :id")
    suspend fun getEstablishmentById(id: Int): Establishment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEstablishment(establishment: Establishment): Long

    // Offerings
    @Query("SELECT * FROM offerings")
    fun getAllOfferingsFlow(): Flow<List<Offering>>

    @Query("SELECT * FROM offerings WHERE establishmentId = :establishmentId")
    fun getOfferingsByEstablishmentFlow(establishmentId: Int): Flow<List<Offering>>

    @Query("SELECT * FROM offerings WHERE name LIKE '%' || :query || '%'")
    fun searchOfferingsFlow(query: String): Flow<List<Offering>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffering(offering: Offering): Long

    @Query("DELETE FROM offerings WHERE id = :id")
    suspend fun deleteOfferingById(id: Int)

    // Quotation Requests
    @Query("SELECT * FROM quotation_requests ORDER BY timestamp DESC")
    fun getAllQuotationRequestsFlow(): Flow<List<QuotationRequest>>

    @Query("SELECT * FROM quotation_requests WHERE id = :id")
    suspend fun getQuotationRequestById(id: Int): QuotationRequest?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotationRequest(request: QuotationRequest): Long

    @Update
    suspend fun updateQuotationRequest(request: QuotationRequest)

    // Quotation Proposals
    @Query("SELECT * FROM quotation_proposals ORDER BY price ASC")
    fun getAllProposalsFlow(): Flow<List<QuotationProposal>>

    @Query("SELECT * FROM quotation_proposals WHERE requestId = :requestId ORDER BY price ASC")
    fun getProposalsForRequestFlow(requestId: Int): Flow<List<QuotationProposal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProposal(proposal: QuotationProposal): Long
}

// ----------------------------------------------------
// DATABASE CLASS
// ----------------------------------------------------

@Database(
    entities = [
        Establishment::class,
        Offering::class,
        QuotationRequest::class,
        QuotationProposal::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AutoDatabase : RoomDatabase() {
    abstract fun autoDao(): AutoDao

    companion object {
        @Volatile
        private var INSTANCE: AutoDatabase? = null

        fun getDatabase(context: Context): AutoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AutoDatabase::class.java,
                    "autoguia_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// ----------------------------------------------------
// REPOSITORY LAYER (WITH SEEDING)
// ----------------------------------------------------

class AutoRepository(private val autoDao: AutoDao) {

    val establishmentsList: Flow<List<Establishment>> = autoDao.getAllEstablishmentsFlow()
    val offeringsList: Flow<List<Offering>> = autoDao.getAllOfferingsFlow()
    val quotationRequestsList: Flow<List<QuotationRequest>> = autoDao.getAllQuotationRequestsFlow()
    val allProposalsList: Flow<List<QuotationProposal>> = autoDao.getAllProposalsFlow()

    fun getOfferingsForEstablishment(shopId: Int): Flow<List<Offering>> = 
        autoDao.getOfferingsByEstablishmentFlow(shopId)

    fun getProposalsWithDetailsForRequest(requestId: Int): Flow<List<RichProposal>> {
        // We combine the proposals flow with establishments
        return kotlinx.coroutines.flow.combine(
            autoDao.getProposalsForRequestFlow(requestId),
            establishmentsList
        ) { proposals, shops ->
            proposals.map { prop ->
                val shop = shops.find { it.id == prop.establishmentId }
                RichProposal(
                    proposal = prop,
                    shopName = shop?.name ?: "Estabelecimento Desconhecido",
                    shopPhone = shop?.phone ?: "",
                    shopType = shop?.type ?: "OFICINA",
                    shopRating = shop?.rating ?: 4.5f
                )
            }
        }
    }

    suspend fun getEstablishment(id: Int): Establishment? = autoDao.getEstablishmentById(id)

    suspend fun registerEstablishment(establishment: Establishment): Int {
        return autoDao.insertEstablishment(establishment).toInt()
    }

    suspend fun addOffering(offering: Offering) {
        autoDao.insertOffering(offering)
    }

    suspend fun deleteOffering(id: Int) {
        autoDao.deleteOfferingById(id)
    }

    suspend fun createQuotationRequest(request: QuotationRequest): Int {
        return autoDao.insertQuotationRequest(request).toInt()
    }

    suspend fun updateQuotationRequest(request: QuotationRequest) {
        autoDao.updateQuotationRequest(request)
    }

    suspend fun submitProposal(proposal: QuotationProposal) {
        autoDao.insertProposal(proposal)
    }

    // Pro-actively prepopulate database with standard, beautiful Brazilian automotive seeds if empty
    suspend fun prepopulateIfEmpty() {
        // Read current list to verify if empty
        val currentShops = establishmentsList.first()
        if (currentShops.isEmpty()) {
            // Seed Establishments
            val toninhoId = autoDao.insertEstablishment(
                Establishment(
                    name = "Oficina do Toninho",
                    type = "OFICINA",
                    address = "Av. Higienópolis, 1420 - Centro",
                    phone = "5511988881111",
                    specialties = "Freios, Retífica, Suspensão, Motores",
                    rating = 4.8f,
                    reviewsCount = 42
                )
            ).toInt()

            val autotechId = autoDao.insertEstablishment(
                Establishment(
                    name = "Mecânica AutoTech",
                    type = "OFICINA",
                    address = "Rua Clélia, 580 - Lapa",
                    phone = "5511988882222",
                    specialties = "Alinhamento, Injeção Eletrônica, Elétrica",
                    rating = 4.6f,
                    reviewsCount = 28
                )
            ).toInt()

            val paranaId = autoDao.insertEstablishment(
                Establishment(
                    name = "Paraná Auto Peças",
                    type = "AUTO_PECAS",
                    address = "Av. Nazaré, 890 - Ipiranga",
                    phone = "5511988883333",
                    specialties = "Peças Novas, Filtros, Velas, Correias, Amortecedores",
                    rating = 4.7f,
                    reviewsCount = 55
                )
            ).toInt()

            val alemaoId = autoDao.insertEstablishment(
                Establishment(
                    name = "Sucata do Alemão",
                    type = "SUCATA",
                    address = "Rua do Galpão, 34 - Santo Amaro",
                    phone = "5511988884444",
                    specialties = "Peças Usadas Originais, Lataria, Motores c/ Baido Detran",
                    rating = 4.4f,
                    reviewsCount = 19
                )
            ).toInt()

            val frenchId = autoDao.insertEstablishment(
                Establishment(
                    name = "Só Peças Francesas",
                    type = "SUCATA",
                    address = "Av. dos Estados, 4400 - Utinga",
                    phone = "5511988885555",
                    specialties = "Peças Peugeot, Citroën, Renault, Câmbio, Módulos",
                    rating = 4.5f,
                    reviewsCount = 15
                )
            ).toInt()

            // Seed Offerings (parts / services)
            // Toninho Services
            autoDao.insertOffering(Offering(establishmentId = toninhoId, name = "Troca de Pastilha de Freio", type = "SERVICE", price = 120.00, condition = "N/A", description = "Mão de obra profissional para troca de pastilhas dianteiras ou traseiras."))
            autoDao.insertOffering(Offering(establishmentId = toninhoId, name = "Troca de Óleo + Filtro", type = "SERVICE", price = 80.00, condition = "N/A", description = "Serviço rápido de substituição de lubrificante e filtro do motor."))
            autoDao.insertOffering(Offering(establishmentId = toninhoId, name = "Retífica de Discos de Freio", type = "SERVICE", price = 150.00, condition = "N/A", description = "Faceamento de discos de freio para eliminar vibrações ao pisar no pedal."))
            
            // AutoTech Services
            autoDao.insertOffering(Offering(establishmentId = autotechId, name = "Alinhamento e Balanceamento 3D", type = "SERVICE", price = 110.00, condition = "N/A", description = "Alinhamento a laser completo e balanceamento das 4 rodas."))
            autoDao.insertOffering(Offering(establishmentId = autotechId, name = "Diagnóstico com Scanner da Injeção", type = "SERVICE", price = 90.00, condition = "N/A", description = "Leitura de códigos de falha e diagnóstico de sensores do motor."))
            
            // Paraná Auto Peças (New Parts)
            autoDao.insertOffering(Offering(establishmentId = paranaId, name = "Pastilha de Freio Cobreq (Dianteira)", type = "PART", price = 145.00, condition = "Nova", description = "Par de pastilhas de excelente durabilidade para Onix/HB20."))
            autoDao.insertOffering(Offering(establishmentId = paranaId, name = "Kit Correia Dentada Gates - Gol G5", type = "PART", price = 185.00, condition = "Nova", description = "Correia de transmissão reforçada e tensionador de marca Premium."))
            autoDao.insertOffering(Offering(establishmentId = paranaId, name = "Amortecedor Dianteiro Cofap - Onix", type = "PART", price = 310.00, condition = "Nova", description = "Unidade do amortecedor a gás pressurizado Cofap de reposição original."))
            autoDao.insertOffering(Offering(establishmentId = paranaId, name = "Jogo de Velas de Ignição NGK", type = "PART", price = 115.00, condition = "Nova", description = "Kit com 4 velas NGK originais para motores Flex modernos."))

            // Sucata do Alemão (Used Parts)
            autoDao.insertOffering(Offering(establishmentId = alemaoId, name = "Porta Dianteira Esquerda HB20 2019", type = "PART", price = 480.00, condition = "Usada", description = "Porta original seminova, alinhada, na cor branca."))
            autoDao.insertOffering(Offering(establishmentId = alemaoId, name = "Alternador Bosch Corolla 1.8 2017", type = "PART", price = 380.00, condition = "Usada", description = "Alternador original inspecionado em bancada com 3 meses de garantia."))
            autoDao.insertOffering(Offering(establishmentId = alemaoId, name = "Farol Direito Honda Civic 2015", type = "PART", price = 320.00, condition = "Usada", description = "Farol original fume sem trincas, presilhas íntegras."))

            // Só Peças Francesas
            autoDao.insertOffering(Offering(establishmentId = frenchId, name = "Módulo de ABS Peugeot 208 2018", type = "PART", price = 650.00, condition = "Usada", description = "Unidade hidráulica original testada c/ nota fiscal para rastreabilidade."))

            // Seed some active requests and proposals to demonstrate the interactive cotação system
            val req1Id = autoDao.insertQuotationRequest(
                QuotationRequest(
                    clientName = "Renato S.",
                    carModel = "Ford Ka 2017 1.0",
                    itemDescription = "Compressor do Ar Condicionado",
                    category = "PART",
                    contactPhone = "5511977771234"
                )
            ).toInt()

            val req2Id = autoDao.insertQuotationRequest(
                QuotationRequest(
                    clientName = "Aline Melo",
                    carModel = "Hyundai HB20 2015",
                    itemDescription = "Troca de amortecedores dianteiros (peça + serviço)",
                    category = "SERVICE",
                    contactPhone = "5511977775678"
                )
            ).toInt()

            // Seed Proposals for Renato: Ka Compressor
            autoDao.insertProposal(
                QuotationProposal(
                    requestId = req1Id,
                    establishmentId = alemaoId,
                    price = 450.00,
                    condition = "Usada",
                    notes = "Compressor em perfeito estado, retirado de Ka blindado batido na traseira. Garantia de 90 dias.",
                    estimatedDays = 1
                )
            )

            autoDao.insertProposal(
                QuotationProposal(
                    requestId = req1Id,
                    establishmentId = paranaId,
                    price = 1200.00,
                    condition = "Nova",
                    notes = "Compressor Mahle original novo lacrado na caixa. Emitimos cupom fiscal.",
                    estimatedDays = 2
                )
            )

            // Seed Proposals for Aline: HB20 Amortecedores
            autoDao.insertProposal(
                QuotationProposal(
                    requestId = req2Id,
                    establishmentId = toninhoId,
                    price = 850.00,
                    condition = "N/A",
                    notes = "Serviço completo: Amortecedores dianteiros novos Cofap, batentes novos + instalação profissional com alinhamento incluso e garantia de 1 ano.",
                    estimatedDays = 1
                )
            )
        }
    }
}
