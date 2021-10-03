package digital.porsche.ks.repositories

import digital.porsche.ks.model.ProductProfitHistory
import org.springframework.data.repository.Repository

interface ProductProfitHistoryRepository : Repository<ProductProfitHistory, String> {

    fun save(productProfitHistory:ProductProfitHistory) : ProductProfitHistory

    fun saveAll(profits: Iterable<ProductProfitHistory>) : Iterable<ProductProfitHistory>

    fun findByTimeFromGreaterThanEqualAndTimeToLessThanEqual(timeFrom: Long, timeTo: Long) : List<ProductProfitHistory>
}
