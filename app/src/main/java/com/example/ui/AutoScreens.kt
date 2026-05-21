package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import java.text.NumberFormat
import java.util.Locale

// ----------------------------------------------------
// THEME COLORS (High Density Light Material 3 Theme)
// ----------------------------------------------------
val DarkBackground = Color(0xFFFEF7FF)        // Scaffold background (#FEF7FF)
val DarkSurface = Color(0xFFFFFFFF)           // Inside cards / dialog surfaces
val DarkSurfaceVariant = Color(0xFFF3EDEF)    // Search bar bg / border line references
val AmberAccent = Color(0xFF6750A4)           // Main primary accent purple (#6750A4)
val AmberAccentDark = Color(0xFF21005D)       // Contrast dark purple text (#21005D)
val DarkOnBackground = Color(0xFF1D1B20)      // Main body text (#1D1B20)
val GrayText = Color(0xFF49454F)              // Secondary descriptive text (#49454F)
val GreenActive = Color(0xFF007A3E)           // WhatsApp/Success green for better visibility
val BorderColor = Color(0xFFCAC4D0)           // Border and outline references

enum class AppTab {
    DIRECTORY,  // Guia de Lojas
    COMPARE,    // Buscar Peças/Preços
    QUOTES,     // Cotações / Pedidos
    PARTNER     // Painel do Lojista
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoGuiaApp(viewModel: AutoViewModel) {
    val context = LocalContext.current
    var currentTab by remember { mutableStateOf(AppTab.DIRECTORY) }
    
    // Bottom Sheet context for showing detailed shop catalog
    var selectedShopIdForDetail by remember { mutableStateOf<Int?>(null) }
    val selectedShop by viewModel.selectedEstablishment.collectAsStateWithLifecycle()
    val selectedShopOfferings by viewModel.selectedEstablishmentOfferings.collectAsStateWithLifecycle()

    // Trigger loading of selected establishment when change
    LaunchedEffect(selectedShopIdForDetail) {
        viewModel.selectEstablishment(selectedShopIdForDetail)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageName("Build"), 
                            contentDescription = "Logo", 
                            tint = AmberAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AUTOGUIA",
                            color = DarkOnBackground,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            fontSize = 20.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = DarkOnBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                // Tab 1: Directory
                NavigationBarItem(
                    selected = currentTab == AppTab.DIRECTORY,
                    onClick = { currentTab = AppTab.DIRECTORY },
                    modifier = Modifier.testTag("tab_btn_directory"),
                    icon = { Icon(Icons.Default.Home, contentDescription = "Oficinas") },
                    label = { Text("Lojas & Oficinas", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkBackground,
                        selectedTextColor = AmberAccent,
                        indicatorColor = AmberAccent,
                        unselectedIconColor = GrayText,
                        unselectedTextColor = GrayText
                    )
                )

                // Tab 2: Compare Prices
                NavigationBarItem(
                    selected = currentTab == AppTab.COMPARE,
                    onClick = { currentTab = AppTab.COMPARE },
                    modifier = Modifier.testTag("tab_btn_compare"),
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Consultar Peças") },
                    label = { Text("Consultar Peças", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkBackground,
                        selectedTextColor = AmberAccent,
                        indicatorColor = AmberAccent,
                        unselectedIconColor = GrayText,
                        unselectedTextColor = GrayText
                    )
                )

                // Tab 3: Quotes / Budgets
                NavigationBarItem(
                    selected = currentTab == AppTab.QUOTES,
                    onClick = { currentTab = AppTab.QUOTES },
                    modifier = Modifier.testTag("tab_btn_quotes"),
                    icon = { Icon(Icons.Default.List, contentDescription = "Cotações") },
                    label = { Text("Cotações", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkBackground,
                        selectedTextColor = AmberAccent,
                        indicatorColor = AmberAccent,
                        unselectedIconColor = GrayText,
                        unselectedTextColor = GrayText
                    )
                )

                // Tab 4: Partner area
                NavigationBarItem(
                    selected = currentTab == AppTab.PARTNER,
                    onClick = { currentTab = AppTab.PARTNER },
                    modifier = Modifier.testTag("tab_btn_partner"),
                    icon = { Icon(Icons.Default.Person, contentDescription = "Parceiro") },
                    label = { Text("Área Lojista", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DarkBackground,
                        selectedTextColor = AmberAccent,
                        indicatorColor = AmberAccent,
                        unselectedIconColor = GrayText,
                        unselectedTextColor = GrayText
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.DIRECTORY -> DirectoryScreen(viewModel) { id ->
                    selectedShopIdForDetail = id
                }
                AppTab.COMPARE -> ComparePricesScreen(viewModel)
                AppTab.QUOTES -> QuotesScreen(viewModel)
                AppTab.PARTNER -> PartnerScreen(viewModel)
            }

            // Bottom Dialog / Sheet for Shop Inventory Detail
            selectedShop?.let { shop ->
                ShopDetailBottomSheet(
                    shop = shop,
                    offerings = selectedShopOfferings,
                    onDismiss = { selectedShopIdForDetail = null }
                )
            }
        }
    }
}

// Helper to resolve safe built-in icons
@Composable
fun imageName(name: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (name) {
        "Build" -> Icons.Default.Build
        "Location" -> Icons.Default.LocationOn
        "Phone" -> Icons.Default.Phone
        "Star" -> Icons.Default.Star
        "Check" -> Icons.Default.Check
        "Delete" -> Icons.Default.Delete
        "Plus" -> Icons.Default.Add
        "User" -> Icons.Default.Person
        "ArrowLeft" -> Icons.Default.ArrowBack
        "Info" -> Icons.Default.Info
        "Send" -> Icons.Default.Send
        "Clock" -> Icons.Default.Settings
        else -> Icons.Default.Build
    }
}

// ----------------------------------------------------
// TAB 1: DIRECTORY SCREEN
// ----------------------------------------------------
@Composable
fun DirectoryScreen(viewModel: AutoViewModel, onShopDetailClick: (Int) -> Unit) {
    val query by viewModel.establishmentSearchQuery.collectAsStateWithLifecycle()
    val filterType by viewModel.selectedShopTypeFilter.collectAsStateWithLifecycle()
    val shops by viewModel.filteredEstablishments.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search header
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.establishmentSearchQuery.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("directory_search_input"),
            label = { Text("Buscar oficina, auto peças, sucata...", color = GrayText) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = DarkOnBackground,
                unfocusedTextColor = DarkOnBackground,
                focusedBorderColor = AmberAccent,
                unfocusedBorderColor = BorderColor,
                focusedContainerColor = DarkSurfaceVariant,
                unfocusedContainerColor = DarkSurfaceVariant
            ),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = GrayText) },
            shape = RoundedCornerShape(28.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Filters scroll
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filterOptions = listOf(
                "ALL" to "Todos",
                "OFICINA" to "Oficinas",
                "AUTO_PECAS" to "Auto Peças",
                "SUCATA" to "Sucatas"
            )

            filterOptions.forEach { (key, label) ->
                val isSelected = filterType == key
                InputChip(
                    selected = isSelected,
                    onClick = { viewModel.selectedShopTypeFilter.value = key },
                    label = { Text(label, color = if (isSelected) DarkBackground else DarkOnBackground) },
                    colors = InputChipDefaults.inputChipColors(
                        containerColor = DarkSurface,
                        selectedContainerColor = AmberAccent
                    ),
                    border = BorderStroke(1.dp, if (isSelected) AmberAccent else BorderColor),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (shops.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Não encontrado",
                        tint = GrayText,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Nenhum estabelecimento encontrado.", color = GrayText, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(shops) { shop ->
                    EstablishmentCard(shop = shop, onDetailClick = { onShopDetailClick(shop.id) })
                }
            }
        }
    }
}

@Composable
fun EstablishmentCard(shop: Establishment, onDetailClick: () -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onDetailClick)
            .testTag("shop_card_${shop.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, DarkSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Name & Type Tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = shop.name,
                    color = DarkOnBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )

                val badgeColor = when (shop.type) {
                    "OFICINA" -> Color(0xFF1E88E5)
                    "AUTO_PECAS" -> Color(0xFF43A047)
                    else -> Color(0xFFE53935) // SUCATA
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, badgeColor)
                ) {
                    Text(
                        text = shop.typeLabel(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = badgeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Address & Specialty
            Text(
                text = "📍 " + shop.address,
                color = GrayText,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "🛠️ Esp: " + shop.specialties,
                color = GrayText,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = DarkSurfaceVariant)

            Spacer(modifier = Modifier.height(12.dp))

            // Rating & Action footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageName("Star"), contentDescription = "Estrela", tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp)) // Let's keep star color beautiful gold/amber 0xFFFFB300
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${shop.rating}",
                        color = AmberAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(${shop.reviewsCount} avaliações)",
                        color = GrayText,
                        fontSize = 12.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledIconButton(
                        onClick = {
                            val contactMessage = "Olá, vi a sua loja ${shop.name} no AutoGuia e gostaria de fazer uma consulta."
                            openWhatsLink(context, shop.phone, contactMessage)
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = GreenActive),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(imageName("Phone"), contentDescription = "ZAP", tint = Color.White, modifier = Modifier.size(18.dp))
                    }

                    Button(
                        onClick = onDetailClick,
                        colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Ver Catálogo", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 2: COMPARE PRICES SCREEN (Direct catalog matching)
// ----------------------------------------------------
@Composable
fun ComparePricesScreen(viewModel: AutoViewModel) {
    val query by viewModel.partServiceSearchQuery.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedPartServiceTab.collectAsStateWithLifecycle()
    val conditionFilter by viewModel.selectedConditionFilter.collectAsStateWithLifecycle()
    val offerings by viewModel.filteredOfferings.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search
        OutlinedTextField(
            value = query,
            onValueChange = { viewModel.partServiceSearchQuery.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("catalog_search_input"),
            label = { Text("O que você procura? (ex: pastilha, amortecedor...)", color = GrayText) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = DarkOnBackground,
                unfocusedTextColor = DarkOnBackground,
                focusedBorderColor = AmberAccent,
                unfocusedBorderColor = BorderColor,
                focusedContainerColor = DarkSurfaceVariant,
                unfocusedContainerColor = DarkSurfaceVariant
            ),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = GrayText) },
            shape = RoundedCornerShape(28.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tabs Row: All, Auto parts (parts), Workshops (services)
        TabRow(
            selectedTabIndex = when (selectedTab) {
                "ALL" -> 0
                "PART" -> 1
                else -> 2
            },
            containerColor = DarkSurface,
            contentColor = AmberAccent,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[when (selectedTab) {
                        "ALL" -> 0
                        "PART" -> 1
                        else -> 2
                    }]),
                    color = AmberAccent
                )
            }
        ) {
            Tab(selected = selectedTab == "ALL", onClick = { viewModel.selectedPartServiceTab.value = "ALL" }) {
                Text("Tudo", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedTab == "PART", onClick = { viewModel.selectedPartServiceTab.value = "PART" }) {
                Text("Peças", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
            }
            Tab(selected = selectedTab == "SERVICE", onClick = { viewModel.selectedPartServiceTab.value = "SERVICE" }) {
                Text("Mão de Obra / Serviços", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Condition filter chips
        if (selectedTab != "SERVICE") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val conditionOptions = listOf(
                    "ALL" to "Minha Condição: Qualquer",
                    "Nova" to "Somente Novas",
                    "Usada" to "Seminovo / Sucata"
                )
                conditionOptions.forEach { (key, label) ->
                    val isSelected = conditionFilter == key
                    InputChip(
                        selected = isSelected,
                        onClick = { viewModel.selectedConditionFilter.value = key },
                        label = { Text(label, color = if (isSelected) DarkBackground else DarkOnBackground) },
                        colors = InputChipDefaults.inputChipColors(
                            containerColor = DarkSurface,
                            selectedContainerColor = AmberAccent
                        ),
                        border = BorderStroke(1.dp, if (isSelected) AmberAccent else BorderColor)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (offerings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Vazio",
                        tint = GrayText,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Nenhuma peça ou serviço cadastrado.", color = GrayText, fontSize = 14.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(offerings) { item ->
                    OfferingCompareCard(itemWithShop = item)
                }
            }
        }
    }
}

@Composable
fun OfferingCompareCard(itemWithShop: OfferingWithShop) {
    val context = LocalContext.current
    val item = itemWithShop.offering
    val shop = itemWithShop.establishment

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("offering_card_${item.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, DarkSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Item Name & Category badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name,
                    color = DarkOnBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )

                // Price tag
                Text(
                    text = formatBRL(item.price),
                    color = AmberAccent,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle information
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val catLabel = if (item.type == "PART") "Peça" else "Serviço"
                val badgeColor = if (item.type == "PART") Color(0xFFE040FB) else Color(0xFF00E5FF)

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = badgeColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = catLabel,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        color = badgeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }

                if (item.type == "PART") {
                    val conditionColor = if (item.condition == "Nova") GreenActive else Color(0xFFFF9100)
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = conditionColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = item.condition,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = conditionColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (item.description.isNotEmpty()) {
                Text(
                    text = item.description,
                    color = GrayText,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Divider(color = DarkSurfaceVariant)

            Spacer(modifier = Modifier.height(10.dp))

            // Seller Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = shop?.name ?: "Vendedor desconhecido",
                        color = DarkOnBackground,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = shop?.address ?: "",
                        color = GrayText,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Call WhatsApp button
                Button(
                    onClick = {
                        val msg = "Olá ${shop?.name}, vi o anúncio da peça/serviço '${item.name}' por ${formatBRL(item.price)} no app AutoGuia e gostaria de fechar."
                        openWhatsLink(context, shop?.phone ?: "", msg)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenActive),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(imageName("Phone"), contentDescription = "ZAP", tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pedir", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 3: QUOTATION REQUEST SYSTEM (COTAÇÕES)
// ----------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotesScreen(viewModel: AutoViewModel) {
    val requests by viewModel.quotationRequests.collectAsStateWithLifecycle()
    var showCreateRequestDialog by remember { mutableStateOf(false) }

    // Request detail sheet trigger
    val activeDetailId by viewModel.activeDetailRequestId.collectAsStateWithLifecycle()
    val activeReqDetails by viewModel.activeRequestDetails.collectAsStateWithLifecycle()
    val activeProposals by viewModel.activeRequestProposals.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Quadro de Cotações",
                    color = DarkOnBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text = "Acompanhe as propostas ou crie uma cotação.",
                    color = GrayText,
                    fontSize = 12.sp
                )
            }

            Button(
                onClick = { showCreateRequestDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("btn_create_quote_request")
            ) {
                Icon(imageName("Plus"), contentDescription = "Adicionar", tint = DarkBackground)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Pedir Preço", color = DarkBackground, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (requests.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = "Cotações Vazias",
                        tint = GrayText,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Nenhuma cotação criada ainda.", color = GrayText, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Toque em 'PEDIR PREÇOS' no topo para iniciar!", color = GrayText, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(requests) { req ->
                    // Count proposals for this specific id
                    QuotationRequestCard(
                        request = req,
                        proposalsCount = 0, // dynamic count
                        onClick = { viewModel.selectQuotationRequestDetail(req.id) }
                    )
                }
            }
        }
    }

    // Modal to create a new quotation request
    if (showCreateRequestDialog) {
        CreateRequestDialog(
            onSubmit = { client, model, desc, category, phone ->
                viewModel.addQuotationRequest(client, model, desc, category, phone) {
                    showCreateRequestDialog = false
                }
            },
            onDismiss = { showCreateRequestDialog = false }
        )
    }

    // Modal to view detail quotes & responses
    activeReqDetails?.let { req ->
        QuotationDetailScreen(
            request = req,
            proposals = activeProposals,
            onCloseRequest = { viewModel.closeQuotationRequest(req.id) },
            onDismiss = { viewModel.selectQuotationRequestDetail(null) }
        )
    }
}

@Composable
fun QuotationRequestCard(request: QuotationRequest, proposalsCount: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("quote_request_card_${request.id}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, DarkSurfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.itemDescription,
                        color = DarkOnBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Veículo: ${request.carModel}",
                        color = GrayText,
                        fontSize = 13.sp
                    )
                }

                val statusColor = if (request.status == "OPEN") GreenActive else GrayText
                val statusText = if (request.status == "OPEN") "Em Aberto" else "Finalizada"
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, statusColor)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Cliente: ${request.clientName}",
                        color = GrayText,
                        fontSize = 12.sp
                    )
                }

                // Redundant check/UI for response summary
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp, 
                        contentDescription = "Mãozinha", 
                        tint = AmberAccent, 
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Ver Propostas Recebidas",
                        color = AmberAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun CreateRequestDialog(
    onSubmit: (client: String, model: String, desc: String, category: String, phone: String) -> Unit,
    onDismiss: () -> Unit
) {
    var clientName by remember { mutableStateOf("") }
    var carModel by remember { mutableStateOf("") }
    var itemDescription by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("PART") } // "PART", "SERVICE"
    var phone by remember { mutableStateOf("") }

    var hasError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("dialog_create_request"),
            border = BorderStroke(1.dp, DarkSurfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Pedir Orçamentos / Cotações",
                    color = DarkOnBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "A sua solicitação será enviada para todas as oficinas e autopeças cadastradas na plataforma.",
                    color = GrayText,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Seu Nome") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DarkOnBackground, unfocusedTextColor = DarkOnBackground, focusedBorderColor = AmberAccent, unfocusedBorderColor = BorderColor
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("req_input_client_name")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = carModel,
                    onValueChange = { carModel = it },
                    label = { Text("Modelo do Carro & Ano (Ex: HB20 2017)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DarkOnBackground, unfocusedTextColor = DarkOnBackground, focusedBorderColor = AmberAccent, unfocusedBorderColor = BorderColor
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("req_input_car_model")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = itemDescription,
                    onValueChange = { itemDescription = it },
                    label = { Text("Peça ou Serviço que precisa pagar menos") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DarkOnBackground, unfocusedTextColor = DarkOnBackground, focusedBorderColor = AmberAccent, unfocusedBorderColor = BorderColor
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("req_input_description")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("WhatsApp p/ receber contato (Ex: 11999999999)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DarkOnBackground, unfocusedTextColor = DarkOnBackground, focusedBorderColor = AmberAccent, unfocusedBorderColor = BorderColor
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("req_input_phone")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = category == "PART",
                            onClick = { category = "PART" },
                            colors = RadioButtonDefaults.colors(selectedColor = AmberAccent)
                        )
                        Text("Peça", color = DarkOnBackground, fontSize = 14.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = category == "SERVICE",
                            onClick = { category = "SERVICE" },
                            colors = RadioButtonDefaults.colors(selectedColor = AmberAccent)
                        )
                        Text("Serviço Oficinas", color = DarkOnBackground, fontSize = 14.sp)
                    }
                }

                if (hasError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Favor preencher todos os campos.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = GrayText)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (clientName.isBlank() || carModel.isBlank() || itemDescription.isBlank() || phone.isBlank()) {
                                hasError = true
                            } else {
                                onSubmit(clientName, carModel, itemDescription, category, phone)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("dialog_submit_request_btn")
                    ) {
                        Text("Lançar No Quadro", color = DarkBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun QuotationDetailScreen(
    request: QuotationRequest,
    proposals: List<RichProposal>,
    onCloseRequest: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("dialog_quote_detail"),
            border = BorderStroke(1.dp, DarkSurfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Cotação Detalhada",
                        color = AmberAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar modal", tint = DarkOnBackground)
                    }
                }

                // Request specification
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = request.itemDescription,
                            color = DarkOnBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Modelo: ${request.carModel}", color = GrayText, fontSize = 12.sp)
                        Text("Cliente: ${request.clientName} | Zap: ${request.contactPhone}", color = GrayText, fontSize = 12.sp)
                        Text("Tipo: ${if (request.category == "PART") "Peça" else "Serviço"}", color = GrayText, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Propostas Recebidas (${proposals.size})",
                    color = DarkOnBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (proposals.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Nenhuma proposta enviada pelos lojistas ainda. As oficinas locais enviam propostas no Painel do Lojista.",
                            color = GrayText,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(proposals) { rich ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DarkBackground),
                                border = BorderStroke(1.dp, DarkSurface)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            rich.shopName,
                                            color = DarkOnBackground,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            "Condição: ${rich.proposal.condition} | Entrega: ${rich.proposal.estimatedDays} dia(s)",
                                            color = GrayText,
                                            fontSize = 11.sp
                                        )
                                        if (rich.proposal.notes.isNotEmpty()) {
                                            Text(
                                                rich.proposal.notes,
                                                color = GrayText,
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            formatBRL(rich.proposal.price),
                                            color = AmberAccent,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Button(
                                            onClick = {
                                                val msg = "Olá ${rich.shopName}, vi sua oferta de ${formatBRL(rich.proposal.price)} para meu pedido de '${request.itemDescription}' do ¹Carro: ${request.carModel} na plataforma AutoGuia. Vamos negociar?"
                                                openWhatsLink(context, rich.shopPhone, msg)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = GreenActive),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("Negociar", fontSize = 10.sp, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (request.status == "OPEN") {
                        TextButton(
                            onClick = {
                                onCloseRequest()
                                Toast.makeText(context, "Solicitação fechada com sucesso!", Toast.LENGTH_SHORT).show()
                            },
                        ) {
                            Text("Fechar Solicitação", color = MaterialTheme.colorScheme.error)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = AmberAccent)
                    ) {
                        Text("Voltar", color = DarkBackground)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 4: PARTNER / BOARD SCREEN (PAINEL DO PARCEIRO)
// ----------------------------------------------------
@Composable
fun PartnerScreen(viewModel: AutoViewModel) {
    val managedShopId by viewModel.managedEstablishmentId.collectAsStateWithLifecycle()
    val shops by viewModel.establishments.collectAsStateWithLifecycle()
    val managedShop by viewModel.managedEstablishment.collectAsStateWithLifecycle()
    val managedOfferings by viewModel.managedEstablishmentOfferings.collectAsStateWithLifecycle()
    val candidateRequests by viewModel.candidateClientRequests.collectAsStateWithLifecycle()

    var showCreateShopDialog by remember { mutableStateOf(false) }
    var activeBidQuoteRequest by remember { mutableStateOf<QuotationRequest?>(null) }
    var showAddCatalogDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (managedShopId == null) {
            // Screen 1: Choose Shop to Manage
            Text(
                text = "Área do Parceiro / Lojista",
                color = DarkOnBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            Text(
                text = "Escolha um estabelecimento cadastrado para gerenciar o estoque e responder a cotações abertas dos clientes da cidade.",
                color = GrayText,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { showCreateShopDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("partner_register_new_shop_btn")
            ) {
                Icon(imageName("Plus"), contentDescription = "Add", tint = DarkBackground)
                Spacer(modifier = Modifier.width(4.dp))
                Text("CADASTRAR NOVA LOJA / OFICINA", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Seus Estabelecimentos Disponíveis",
                color = DarkOnBackground,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (shops.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("Sem lojas cadastradas. Cadastre a sua acima!", color = GrayText)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(shops) { shop ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("select_shop_card_${shop.id}"),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface),
                            border = BorderStroke(1.dp, DarkSurfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(shop.name, color = DarkOnBackground, fontWeight = FontWeight.Bold)
                                    Text(shop.typeLabel(), color = AmberAccent, fontSize = 12.sp)
                                    Text(shop.address, color = GrayText, fontSize = 11.sp, maxLines = 1)
                                }

                                Button(
                                    onClick = { viewModel.setManagedShop(shop.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("btn_manage_shop_${shop.id}")
                                ) {
                                    Text("Gerenciar", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Screen 2: Dashboard screen once Chosen
            managedShop?.let { shop ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = shop.name,
                            color = DarkOnBackground,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Painel de Controle: ${shop.typeLabel()}",
                            color = AmberAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TextButton(
                        onClick = { viewModel.managedEstablishmentId.value = null }
                    ) {
                        Text("Mudar Loja", color = GrayText, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                var currentSubTab by remember { mutableStateOf(0) } // 0: Nosso Catálogo, 1: Painel de Cotações

                TabRow(
                    selectedTabIndex = currentSubTab,
                    containerColor = DarkSurface,
                    contentColor = AmberAccent,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[currentSubTab]),
                            color = AmberAccent
                        )
                    }
                ) {
                    Tab(selected = currentSubTab == 0, onClick = { currentSubTab = 0 }) {
                        Text("Estoque / Catálogo", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Tab(selected = currentSubTab == 1, onClick = { currentSubTab = 1 }) {
                        Text("Mandar Propostas", modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                when (currentSubTab) {
                    0 -> { // Catalog Management
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Seu Catálogo (${managedOfferings.size} itens)", color = DarkOnBackground, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Button(
                                onClick = { showAddCatalogDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("btn_show_add_catalog_dialog")
                            ) {
                                Icon(imageName("Plus"), contentDescription = "Add Item", tint = DarkBackground, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Item", color = DarkBackground, fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (managedOfferings.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Nenhum item cadastrado. Lance novos itens no botão de 'Add Item'.", color = GrayText, fontSize = 12.sp)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(managedOfferings) { off ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .padding(12.dp)
                                                .fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(off.name, color = DarkOnBackground, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text(
                                                    text = "Tipo: ${if (off.type == "PART") "Peça (${off.condition})" else "Serviço"} | Preço: ${formatBRL(off.price)}",
                                                    color = GrayText,
                                                    fontSize = 11.sp
                                                )
                                            }

                                            IconButton(
                                                onClick = { viewModel.partnerDeleteOffering(off.id) }
                                            ) {
                                                Icon(imageName("Delete"), contentDescription = "Apagar", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> { // Bidding Client Requests
                        Text(
                            text = "Solicitações Ativas na Cidade",
                            color = DarkOnBackground,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Responda enviando seu melhor preço para atrair o cliente.",
                            color = GrayText,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (candidateRequests.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Nenhum pedido de cotação aberto atualmente.", color = GrayText, fontSize = 12.sp)
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(candidateRequests) { req ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant)
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(req.itemDescription, color = DarkOnBackground, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("Carro: ${req.carModel} | Cliente: ${req.clientName}", color = GrayText, fontSize = 11.sp)
                                            
                                            Spacer(modifier = Modifier.height(10.dp))

                                            Button(
                                                onClick = { activeBidQuoteRequest = req },
                                                colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                                modifier = Modifier
                                                    .align(Alignment.End)
                                                    .height(32.dp)
                                                    .testTag("btn_quote_respond_${req.id}")
                                            ) {
                                                Text("Mandar Preço", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal to register a brand new shop
    if (showCreateShopDialog) {
        CreateShopDialog(
            onSubmit = { name, type, address, phone, specs ->
                viewModel.registerPartnerShop(name, type, address, phone, specs) {
                    showCreateShopDialog = false
                }
            },
            onDismiss = { showCreateShopDialog = false }
        )
    }

    // Modal to send bid proposal
    activeBidQuoteRequest?.let { req ->
        SendProposalDialog(
            request = req,
            onSubmit = { price, cond, notes, days ->
                viewModel.submitPartnerProposal(req.id, price, cond, notes, days) {
                    activeBidQuoteRequest = null
                }
            },
            onDismiss = { activeBidQuoteRequest = null }
        )
    }

    // Modal to add catalog item
    if (showAddCatalogDialog) {
        AddCatalogDialog(
            onSubmit = { name, type, price, cond, desc ->
                viewModel.partnerAddOffering(name, type, price, cond, desc)
                showAddCatalogDialog = false
            },
            onDismiss = { showAddCatalogDialog = false }
        )
    }
}

@Composable
fun CreateShopDialog(
    onSubmit: (name: String, type: String, address: String, phone: String, specs: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("OFICINA") } // "OFICINA", "AUTO_PECAS", "SUCATA"
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var specs by remember { mutableStateOf("") }
    
    var hasError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("dialog_create_shop")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Cadastrar Seu Estabelecimento", color = DarkOnBackground, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome Fantasia") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AmberAccent, unfocusedBorderColor = BorderColor, focusedTextColor = DarkOnBackground, unfocusedTextColor = DarkOnBackground),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("shop_input_name")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Endereço Completo") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AmberAccent, unfocusedBorderColor = BorderColor, focusedTextColor = DarkOnBackground, unfocusedTextColor = DarkOnBackground),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("shop_input_address")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("WhatsApp de Atendimento (Apenas números)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AmberAccent, unfocusedBorderColor = BorderColor, focusedTextColor = DarkOnBackground, unfocusedTextColor = DarkOnBackground),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("shop_input_phone")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = specs,
                    onValueChange = { specs = it },
                    label = { Text("Especialidades (Ex: Amortecedores, Câmbio)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AmberAccent, unfocusedBorderColor = BorderColor, focusedTextColor = DarkOnBackground, unfocusedTextColor = DarkOnBackground),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("shop_input_specialties")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Selector: Type
                Text("Tipo de Estabelecimento", color = DarkOnBackground, fontSize = 13.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("OFICINA" to "Oficina", "AUTO_PECAS" to "Peças", "SUCATA" to "Sucata").forEach { (key, label) ->
                        val isSelected = type == key
                        InputChip(
                            selected = isSelected,
                            onClick = { type = key },
                            label = { Text(label, color = if (isSelected) DarkBackground else DarkOnBackground) },
                            colors = InputChipDefaults.inputChipColors(selectedContainerColor = AmberAccent, containerColor = DarkSurfaceVariant),
                            border = BorderStroke(1.dp, if (isSelected) AmberAccent else Color.Transparent)
                        )
                    }
                }

                if (hasError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Favor preencher todos os dados.", color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar", color = GrayText) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isBlank() || address.isBlank() || phone.isBlank() || specs.isBlank()) {
                                hasError = true
                            } else {
                                onSubmit(name, type, address, phone, specs)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("dialog_shop_submit_btn")
                    ) {
                        Text("Cadastrar", color = DarkBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SendProposalDialog(
    request: QuotationRequest,
    onSubmit: (price: Double, condition: String, notes: String, days: Int) -> Unit,
    onDismiss: () -> Unit
) {
    var priceText by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("Nova") } // "Nova", "Usada", "Original Reciclada", "N/A"
    var notes by remember { mutableStateOf("") }
    var daysText by remember { mutableStateOf("1") }

    var hasError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("dialog_send_proposal")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Enviar Proposta de Orçamento", color = DarkOnBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Para: ${request.clientName} (${request.carModel})", color = AmberAccent, fontSize = 12.sp)
                Text("Item solicitado: ${request.itemDescription}", color = GrayText, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Preço Oferecido (R$)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AmberAccent, unfocusedBorderColor = BorderColor, focusedTextColor = DarkOnBackground, unfocusedTextColor = DarkOnBackground),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("proposal_input_price")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = daysText,
                    onValueChange = { daysText = it },
                    label = { Text("Dias úteis para entrega / conclusão") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AmberAccent, unfocusedBorderColor = BorderColor, focusedTextColor = DarkOnBackground, unfocusedTextColor = DarkOnBackground),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("proposal_input_days")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observações ou detalhes da peça/mão de obra") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AmberAccent, unfocusedBorderColor = BorderColor, focusedTextColor = DarkOnBackground, unfocusedTextColor = DarkOnBackground),
                    modifier = Modifier.fillMaxWidth().testTag("proposal_input_notes")
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (request.category == "PART") {
                    Text("Condição da Peça", color = DarkOnBackground, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Nova" to "Nova", "Usada" to "Seminovo / Usado", "Original Reciclada" to "Reciclada").forEach { (key, label) ->
                            val isSelected = condition == key
                            InputChip(
                                selected = isSelected,
                                onClick = { condition = key },
                                label = { Text(label, color = if (isSelected) DarkBackground else DarkOnBackground) },
                                colors = InputChipDefaults.inputChipColors(selectedContainerColor = AmberAccent, containerColor = DarkSurfaceVariant),
                                border = BorderStroke(1.dp, if (isSelected) AmberAccent else Color.Transparent)
                            )
                        }
                    }
                } else {
                    condition = "N/A"
                }

                if (hasError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Preço e prazos inválidos.", color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar", color = GrayText) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val pr = priceText.toDoubleOrNull()
                            val dy = daysText.toIntOrNull()
                            if (pr == null || dy == null) {
                                hasError = true
                            } else {
                                onSubmit(pr, condition, notes, dy)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("proposal_submit_btn")
                    ) {
                        Text("Mandar Proposta", color = DarkBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun AddCatalogDialog(
    onSubmit: (name: String, type: String, price: Double, condition: String, desc: String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("PART") } // "PART", "SERVICE"
    var priceText by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("Nova") }
    var description by remember { mutableStateOf("") }

    var hasError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("dialog_add_catalog")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Adicionar Item ao Catálogo", color = DarkOnBackground, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da Peça ou Serviço") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AmberAccent, unfocusedBorderColor = BorderColor, focusedTextColor = DarkOnBackground, unfocusedTextColor = DarkOnBackground),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("catalog_input_name")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Preço de Venda (R$)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AmberAccent, unfocusedBorderColor = BorderColor, focusedTextColor = DarkOnBackground, unfocusedTextColor = DarkOnBackground),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("catalog_input_price")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descrição adicional (Modelos compatíveis)") },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AmberAccent, unfocusedBorderColor = BorderColor, focusedTextColor = DarkOnBackground, unfocusedTextColor = DarkOnBackground),
                    modifier = Modifier.fillMaxWidth().testTag("catalog_input_desc")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Selector: PEÇA ou SERVIÇO
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = type == "PART",
                            onClick = { type = "PART" },
                            colors = RadioButtonDefaults.colors(selectedColor = AmberAccent)
                        )
                        Text("Peça Física", color = DarkOnBackground, fontSize = 14.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = type == "SERVICE",
                            onClick = { type = "SERVICE" },
                            colors = RadioButtonDefaults.colors(selectedColor = AmberAccent)
                        )
                        Text("Mão de Obra / Serviço", color = DarkOnBackground, fontSize = 14.sp)
                    }
                }

                if (type == "PART") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Estado de conservação", color = DarkOnBackground, fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Nova" to "Nova", "Usada" to "Seminovo / Usada").forEach { (key, label) ->
                            val isSelected = condition == key
                            InputChip(
                                selected = isSelected,
                                onClick = { condition = key },
                                label = { Text(label, color = if (isSelected) DarkBackground else DarkOnBackground) },
                                colors = InputChipDefaults.inputChipColors(selectedContainerColor = AmberAccent, containerColor = DarkSurfaceVariant),
                                border = BorderStroke(1.dp, if (isSelected) AmberAccent else Color.Transparent)
                            )
                        }
                    }
                } else {
                    condition = "N/A"
                }

                if (hasError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Favor preencher nome e preço válidos.", color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancelar", color = GrayText) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val pr = priceText.toDoubleOrNull()
                            if (name.isBlank() || pr == null) {
                                hasError = true
                            } else {
                                onSubmit(name, type, pr, condition, description)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("catalog_submit_btn")
                    ) {
                        Text("Adicionar", color = DarkBackground, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------
// BOTTOM SHEET DIALOG: INDIVIDUAL SHOP CATALOG DETAILS
// ----------------------------------------------------
@Composable
fun ShopDetailBottomSheet(
    shop: Establishment,
    offerings: List<Offering>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("dialog_shop_detail"),
            border = BorderStroke(1.dp, DarkSurfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                // Main Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(shop.name, color = DarkOnBackground, fontWeight = FontWeight.Black, fontSize = 18.sp)
                        Text(shop.typeLabel(), color = AmberAccent, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar modal", tint = DarkOnBackground)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Specialty and address chips
                Text("📍 " + shop.address, color = GrayText, fontSize = 12.sp)
                Text("📞 Contato: " + formatDisplayPhone(shop.phone), color = GrayText, fontSize = 12.sp)
                Text("🛠️ Especialidades: " + shop.specialties, color = GrayText, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Catálogo / Itens Disponíveis (${offerings.size})",
                        color = DarkOnBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    Button(
                        onClick = {
                            val msg = "Olá, vi sua oficina/loja '${shop.name}' no app AutoGuia e gostaria de ver se realizam orçamentos personalizados."
                            openWhatsLink(context, shop.phone, msg)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenActive),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(imageName("Phone"), contentDescription = "ZAP", tint = Color.White, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WhatsApp", color = Color.White, fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (offerings.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhum item adicionado no catálogo desta loja ainda.",
                            color = GrayText,
                            fontSize = 11.sp
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(offerings) { item ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = DarkBackground)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(10.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.name, color = DarkOnBackground, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(
                                            text = "Tipo: ${if (item.type == "PART") "Peça (${item.condition})" else "Serviço"} - ${item.description}",
                                            color = GrayText,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(formatBRL(item.price), color = AmberAccent, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        FilledTonalButton(
                                            onClick = {
                                                val msg = "Olá, vi à venda '${item.name}' por ${formatBRL(item.price)} em sua loja '${shop.name}' no AutoGuia. Gostaria de comprar/agendar!"
                                                openWhatsLink(context, shop.phone, msg)
                                            },
                                            shape = RoundedCornerShape(4.dp),
                                            colors = ButtonDefaults.filledTonalButtonColors(containerColor = AmberAccent.copy(alpha = 0.15f)),
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                            modifier = Modifier.height(24.dp)
                                        ) {
                                            Text("Comprar", fontSize = 9.sp, color = AmberAccent)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = AmberAccent),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Fechar", color = Color.White)
                }
            }
        }
    }
}

// ----------------------------------------------------
// UTILITIES FUNCTIONS
// ----------------------------------------------------
fun formatBRL(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
    return format.format(amount)
}

fun formatDisplayPhone(raw: String): String {
    // raw format: e.g. 5511988881111 => (11) 98888-1111
    if (raw.length >= 12) {
        val ddd = raw.substring(2, 4)
        val part1 = raw.substring(4, 9)
        val part2 = raw.substring(9)
        return "($ddd) $part1-$part2"
    }
    return raw
}

fun openWhatsLink(context: Context, phone: String, message: String) {
    try {
        // Enforces international format + prefix if missing
        val formattedPhone = if (phone.startsWith("55")) phone else "55$phone"
        val intentUri = Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=" + Uri.encode(message))
        val intent = Intent(Intent.ACTION_VIEW, intentUri)
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Erro ao abrir link de contato.", Toast.LENGTH_SHORT).show()
    }
}
