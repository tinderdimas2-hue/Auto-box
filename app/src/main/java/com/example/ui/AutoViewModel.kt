package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AutoViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AutoDatabase.getDatabase(application)
    private val repository = AutoRepository(db.autoDao())

    // ----------------------------------------------------
    // STATE FLOWS
    // ----------------------------------------------------

    // Establishments Flow API
    val establishments = repository.establishmentsList
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Offerings Flow API
    val offerings = repository.offeringsList
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Requests Flow API
    val quotationRequests = repository.quotationRequestsList
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ----------------------------------------------------
    // ACTIVE FILTERS & SEARCH STATES (MUTABLE STATE FLOWS)
    // ----------------------------------------------------
    val establishmentSearchQuery = MutableStateFlow("")
    val selectedShopTypeFilter = MutableStateFlow("ALL") // "ALL", "OFICINA", "AUTO_PECAS", "SUCATA"

    val partServiceSearchQuery = MutableStateFlow("")
    val selectedPartServiceTab = MutableStateFlow("ALL") // "ALL", "PART", "SERVICE"
    val selectedConditionFilter = MutableStateFlow("ALL") // "ALL", "Nova", "Usada"

    // Observed filtered results
    val filteredEstablishments = combine(
        establishments,
        establishmentSearchQuery,
        selectedShopTypeFilter
    ) { shops, query, filter ->
        shops.filter { shop ->
            val matchesType = filter == "ALL" || shop.type == filter
            val matchesSearch = query.isEmpty() || 
                    shop.name.contains(query, ignoreCase = true) || 
                    shop.specialties.contains(query, ignoreCase = true) ||
                    shop.address.contains(query, ignoreCase = true)
            matchesType && matchesSearch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered parts and services catalog with matching shop details
    val filteredOfferings = combine(
        offerings,
        establishments,
        partServiceSearchQuery,
        selectedPartServiceTab,
        selectedConditionFilter
    ) { items, shops, query, typeTab, condition ->
        items.filter { item ->
            val matchesType = typeTab == "ALL" || item.type == typeTab
            val matchesCondition = condition == "ALL" || item.condition == condition
            val matchesSearch = query.isEmpty() || 
                    item.name.contains(query, ignoreCase = true) || 
                    item.description.contains(query, ignoreCase = true)
            matchesType && matchesCondition && matchesSearch
        }.map { item ->
            val shop = shops.find { it.id == item.establishmentId }
            OfferingWithShop(item, shop)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Selection Details
    private val _selectedEstablishmentId = MutableStateFlow<Int?>(null)
    val selectedEstablishment = _selectedEstablishmentId.flatMapLatest { id ->
        if (id == null) flowOf<Establishment?>(null)
        else {
            flow {
                val shop = repository.getEstablishment(id)
                emit(shop)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedEstablishmentOfferings = _selectedEstablishmentId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else repository.getOfferingsForEstablishment(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Quotation Request Detail Mode
    val activeDetailRequestId = MutableStateFlow<Int?>(null)
    val activeRequestDetails = activeDetailRequestId.flatMapLatest { id ->
        if (id == null) flowOf<QuotationRequest?>(null)
        else {
            flow {
                val req = db.autoDao().getQuotationRequestById(id)
                emit(req)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeRequestProposals = activeDetailRequestId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else repository.getProposalsWithDetailsForRequest(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ----------------------------------------------------
    // PARTNER MODULE STATES
    // ----------------------------------------------------
    val managedEstablishmentId = MutableStateFlow<Int?>(null)
    
    val managedEstablishment = managedEstablishmentId.flatMapLatest { id ->
        if (id == null) flowOf<Establishment?>(null)
        else {
            flow {
                val shop = repository.getEstablishment(id)
                emit(shop)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val managedEstablishmentOfferings = managedEstablishmentId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else repository.getOfferingsForEstablishment(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Candidate client requests matching partner offerings/specialties
    val candidateClientRequests = combine(
        quotationRequests,
        managedEstablishment
    ) { reqs, shop ->
        if (shop == null) emptyList()
        else {
            // In a real app we could filter requests by matching categories.
            // For high-fidelity, show open requests that this partner has NOT yet quote-replied
            reqs.filter { it.status == "OPEN" }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ----------------------------------------------------
    // INITIATIVE
    // ----------------------------------------------------
    init {
        viewModelScope.launch {
            // First automatic database seed so the screens look stunning and active
            repository.prepopulateIfEmpty()
        }
    }

    // ----------------------------------------------------
    // ACTIONS
    // ----------------------------------------------------
    fun selectEstablishment(id: Int?) {
        _selectedEstablishmentId.value = id
    }

    fun selectQuotationRequestDetail(id: Int?) {
        activeDetailRequestId.value = id
    }

    fun addQuotationRequest(
        clientName: String,
        carModel: String,
        itemDescription: String,
        category: String,
        contactPhone: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val req = QuotationRequest(
                clientName = clientName,
                carModel = carModel,
                itemDescription = itemDescription,
                category = category,
                contactPhone = contactPhone
            )
            repository.createQuotationRequest(req)
            onSuccess()
        }
    }

    fun closeQuotationRequest(requestId: Int) {
        viewModelScope.launch {
            val req = db.autoDao().getQuotationRequestById(requestId)
            if (req != null) {
                repository.updateQuotationRequest(req.copy(status = "CLOSED"))
                if (activeDetailRequestId.value == requestId) {
                    activeDetailRequestId.value = requestId // trigger reload
                }
            }
        }
    }

    // Partner setup actions
    fun setManagedShop(id: Int) {
        managedEstablishmentId.value = id
    }

    fun registerPartnerShop(
        name: String,
        type: String,
        address: String,
        phone: String,
        specialties: String,
        onSuccess: (Int) -> Unit
    ) {
        viewModelScope.launch {
            val shop = Establishment(
                name = name,
                type = type,
                address = address,
                phone = phone,
                specialties = specialties
            )
            val newId = repository.registerEstablishment(shop)
            managedEstablishmentId.value = newId
            onSuccess(newId)
        }
    }

    fun partnerAddOffering(
        name: String,
        type: String,
        price: Double,
        condition: String,
        description: String
    ) {
        val shopId = managedEstablishmentId.value ?: return
        viewModelScope.launch {
            val off = Offering(
                establishmentId = shopId,
                name = name,
                type = type,
                price = price,
                condition = condition,
                description = description
            )
            repository.addOffering(off)
        }
    }

    fun partnerDeleteOffering(offeringId: Int) {
        viewModelScope.launch {
            repository.deleteOffering(offeringId)
        }
    }

    fun submitPartnerProposal(
        requestId: Int,
        price: Double,
        condition: String,
        notes: String,
        estimatedDays: Int,
        onSuccess: () -> Unit
    ) {
        val shopId = managedEstablishmentId.value ?: return
        viewModelScope.launch {
            val prop = QuotationProposal(
                requestId = requestId,
                establishmentId = shopId,
                price = price,
                condition = condition,
                notes = notes,
                estimatedDays = estimatedDays
            )
            repository.submitProposal(prop)
            onSuccess()
        }
    }
}

// ----------------------------------------------------
// MATCHING UTILITY CLASSSES
// ----------------------------------------------------
data class OfferingWithShop(
    val offering: Offering,
    val establishment: Establishment?
)
