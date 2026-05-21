package com.sdd.marketplace.feature.product.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sdd.marketplace.core.ui.components.*
import com.sdd.marketplace.core.ui.theme.*
import com.sdd.marketplace.feature.product.viewmodel.PostProductEvent
import com.sdd.marketplace.feature.product.viewmodel.PostProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostProductScreen(
    onNavigateBack: () -> Unit,
    onPostSuccess: () -> Unit,
    viewModel: PostProductViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is PostProductEvent.PostSuccess -> onPostSuccess()
                else -> {}
            }
        }
    }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { viewModel.addImage(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Filled.ArrowBack, "Back") } },
                title = { Text("Post New Product", fontWeight = FontWeight.Bold) },
                actions = { TextButton(onClick = { }) { Text("Preview", color = SddPink) } }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {
            // Step indicator
            LinearProgressIndicator(
                progress = { uiState.step / 3f },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = SddPink
            )
            Text("Step ${uiState.step} of 3", color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)

            if (uiState.step == 1) Step1Content(uiState, viewModel) { imageLauncher.launch("image/*") }
            if (uiState.step == 2) Step2Content(uiState, viewModel)
            if (uiState.step == 3) Step3Content(uiState, viewModel)

            Spacer(Modifier.height(16.dp))

            // Bottom buttons
            Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (uiState.step > 1) {
                    SddOutlineButton("Back", onClick = { viewModel.prevStep() }, modifier = Modifier.weight(1f))
                }
                SddButton(
                    text = if (uiState.step == 3) "Preview Listing" else "Next",
                    onClick = {
                        if (uiState.step < 3) viewModel.nextStep()
                        else viewModel.submitProduct()
                    },
                    isLoading = uiState.isLoading,
                    modifier = Modifier.weight(1f)
                )
            }

            // Safe & Secure notice
            Card(
                Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = SddPink.copy(alpha = 0.05f))
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Security, "Safe", tint = SddPink)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("Safe & Secure", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                        Text("Your listing will be reviewed to ensure a safe marketplace for everyone.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
fun Step1Content(uiState: com.sdd.marketplace.feature.product.viewmodel.PostProductUiState, viewModel: PostProductViewModel, onAddImage: () -> Unit) {
    Column(Modifier.padding(16.dp)) {
        // Photo Section
        if (uiState.selectedImages.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().height(160.dp).clickable { onAddImage() },
                colors = CardDefaults.cardColors(containerColor = SddPink.copy(alpha = 0.05f)),
                border = BorderStroke(2.dp, SddPink.copy(alpha = 0.3f)), shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.Outlined.AddAPhoto, "Add Photo", tint = SddPink, modifier = Modifier.size(36.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Add Photos", color = SddPink, fontWeight = FontWeight.Medium)
                    Text("Add up to 10 photos", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                Box(
                    modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp)).background(SddPink.copy(alpha = 0.1f)).clickable { onAddImage() },
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Filled.Add, "Add", tint = SddPink) }
            }
            items(uiState.selectedImages.size) { i ->
                val uri = uiState.selectedImages[i]
                Box(Modifier.size(70.dp)) {
                    AsyncImage(model = uri, contentDescription = "Image", modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                    IconButton(onClick = { viewModel.removeImage(uri) }, modifier = Modifier.size(20.dp).align(Alignment.TopEnd)) {
                        Icon(Icons.Filled.Close, "Remove", tint = Color.White, modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(10.dp)))
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Basic Information", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(12.dp))
        SddTextField(value = uiState.title, onValueChange = { viewModel.updateTitle(it) }, label = "Product Title", leadingIcon = { Icon(Icons.Outlined.Edit, "Title") })
        Spacer(Modifier.height(12.dp))

        // Category Dropdown
        var catExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = catExpanded, onExpandedChange = { catExpanded = it }) {
            OutlinedTextField(
                value = uiState.category.ifBlank { "Select category" },
                onValueChange = {}, readOnly = true, label = { Text("Category") },
                leadingIcon = { Icon(Icons.Outlined.GridView, "Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SddPink)
            )
            ExposedDropdownMenu(expanded = catExpanded, onDismissRequest = { catExpanded = false }) {
                listOf("Women", "Men", "Home", "Beauty", "Electronics", "Other").forEach { cat ->
                    DropdownMenuItem(text = { Text(cat) }, onClick = { viewModel.updateCategory(cat); catExpanded = false })
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        SddTextField(value = uiState.brand, onValueChange = { viewModel.updateBrand(it) }, label = "Brand (Optional)")
        Spacer(Modifier.height(12.dp))
        // Condition
        var condExpanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = condExpanded, onExpandedChange = { condExpanded = it }) {
            OutlinedTextField(
                value = uiState.condition.ifBlank { "Select condition" },
                onValueChange = {}, readOnly = true, label = { Text("Condition") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = condExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(), shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SddPink)
            )
            ExposedDropdownMenu(expanded = condExpanded, onDismissRequest = { condExpanded = false }) {
                listOf("New", "Like New", "Good", "Fair", "Poor").forEach { cond ->
                    DropdownMenuItem(text = { Text(cond) }, onClick = { viewModel.updateCondition(cond); condExpanded = false })
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Price & Stock", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SddTextField(value = uiState.price, onValueChange = { viewModel.updatePrice(it) }, label = "Price ₹", modifier = Modifier.weight(1f),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal))
            SddTextField(value = uiState.discountPrice, onValueChange = { viewModel.updateDiscountPrice(it) }, label = "Discount ₹ (Opt)", modifier = Modifier.weight(1f),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal))
        }

        Spacer(Modifier.height(16.dp))
        Text("Product Details", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(12.dp))
        SddTextField(value = uiState.description, onValueChange = { viewModel.updateDescription(it) }, label = "Describe your product", singleLine = false)
    }
}

@Composable
fun Step2Content(uiState: com.sdd.marketplace.feature.product.viewmodel.PostProductUiState, viewModel: PostProductViewModel) {
    Column(Modifier.padding(16.dp)) {
        Text("More Details", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(12.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column {
                ListItem(
                    headlineContent = { Text("Location") },
                    supportingContent = { Text(uiState.location.ifBlank { "Select location" }) },
                    leadingContent = { Icon(Icons.Outlined.LocationOn, "Location", tint = SddPink) },
                    trailingContent = { Icon(Icons.Filled.ChevronRight, "") },
                    modifier = Modifier.clickable { }
                )
                Divider(Modifier.padding(horizontal = 16.dp))
                ListItem(
                    headlineContent = { Text("Delivery Options") },
                    supportingContent = { Text(if (uiState.deliveryOptions.isEmpty()) "Select delivery options" else uiState.deliveryOptions.joinToString(", ")) },
                    leadingContent = { Icon(Icons.Outlined.LocalShipping, "Delivery", tint = SddPink) },
                    trailingContent = { Icon(Icons.Filled.ChevronRight, "") },
                    modifier = Modifier.clickable { }
                )
                Divider(Modifier.padding(horizontal = 16.dp))
                ListItem(
                    headlineContent = { Text("Return Policy") },
                    supportingContent = { Text(uiState.returnPolicy.ifBlank { "Select return policy" }) },
                    leadingContent = { Icon(Icons.Outlined.Refresh, "Return", tint = SddPink) },
                    trailingContent = { Icon(Icons.Filled.ChevronRight, "") },
                    modifier = Modifier.clickable { }
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Set a Price", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        Card(colors = CardDefaults.cardColors(containerColor = SddPink.copy(alpha = 0.08f))) {
            ListItem(
                headlineContent = { Text("Set a fair price to attract buyers", fontWeight = FontWeight.Medium) },
                supportingContent = { Text("Research similar products to help you price it right.") },
                leadingContent = { Icon(Icons.Filled.Lightbulb, "", tint = SddPink) }
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { }) {
            RadioButton(selected = !uiState.isNegotiable, onClick = { }, colors = RadioButtonDefaults.colors(selectedColor = SddPink))
            Column {
                Text("Fixed Price", fontWeight = FontWeight.Medium)
                Text("Set a fixed price for your product", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { viewModel.toggleNegotiable() }) {
            RadioButton(selected = uiState.isNegotiable, onClick = { viewModel.toggleNegotiable() }, colors = RadioButtonDefaults.colors(selectedColor = SddPink))
            Column {
                Text("Negotiable", fontWeight = FontWeight.Medium)
                Text("Allow buyers to make offers", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("More Options", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(8.dp))
        Card {
            ListItem(
                headlineContent = { Text("Mark as New") },
                supportingContent = { Text("Product is brand new") },
                leadingContent = { Icon(Icons.Outlined.NewReleases, "New", tint = SddPink) },
                trailingContent = { Switch(checked = uiState.isNew, onCheckedChange = { viewModel.toggleNew() }, colors = SwitchDefaults.colors(checkedThumbColor = SddPink, checkedTrackColor = SddPink.copy(alpha = 0.5f))) }
            )
            Divider(Modifier.padding(horizontal = 16.dp))
            ListItem(
                headlineContent = { Text("Boost Listing") },
                supportingContent = { Text("Increase visibility of your product") },
                leadingContent = { Icon(Icons.Outlined.Bolt, "Boost", tint = SddPink) },
                trailingContent = { Switch(checked = uiState.isBoosted, onCheckedChange = { viewModel.toggleBoosted() }, colors = SwitchDefaults.colors(checkedThumbColor = SddPink, checkedTrackColor = SddPink.copy(alpha = 0.5f))) }
            )
        }
    }
}

@Composable
fun Step3Content(uiState: com.sdd.marketplace.feature.product.viewmodel.PostProductUiState, viewModel: PostProductViewModel) {
    Column(Modifier.padding(16.dp)) {
        Text("Preview", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(12.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text(uiState.title.ifBlank { "Product Title" }, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("₹${uiState.price}", color = SddPink, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                Text(uiState.category, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(uiState.description.take(100), style = MaterialTheme.typography.bodySmall)
            }
        }
        uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }
    }
}
