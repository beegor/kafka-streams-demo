package digital.porsche.ks.domain

data class Shop(
    val id: String,
    val streetAddress: String,
    val city: String
)

fun getShops() = listOf(
    Shop("001", "Winton Place 100", "London"),
//    Shop("002", "Eldon Strasse 200", "Berlin"),
//    Shop("003", "50 rue Beauvau", "Paris"),
//    Shop("004", "Rio Segura 400", "Barcelona"),
)