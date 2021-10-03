package digital.porsche.ks.model

import java.math.BigDecimal
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table


@Entity
@Table(name = "PRODUCT_PROFIT")
class ProductProfitHistory (

    @Id
    val id: String,

    val productId: String,

    val productName: String,

    val amountSold: Int,

    val profit: BigDecimal,

    val timeFrom: Long,

    val timeTo: Long

)