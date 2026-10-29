package com.example.financetracker.data

import kotlinx.coroutines.flow.Flow

/*
 * ҮҮРЭГ: Repository — ViewModel ба өгөгдлийн эх үүсвэр (DAO) хоёрын ДУНДАХ давхарга.
 *
 * Яагаад хэрэгтэй вэ?
 *   ViewModel "өгөгдөл хаанаас ирж байна (Room уу, интернэт үү)" гэдгийг мэдэх шаардлагагүй.
 *   Тэр зөвхөн Repository-оос асууна. Дараа нь эх үүсвэр солигдсон ч ViewModel өөрчлөгдөхгүй.
 *
 * Энд interface (гэрээ), доорх класс нь түүний хэрэгжүүлэлт.
 */

interface TransactionRepository {
    fun getAllStream(): Flow<List<Transaction>>
    fun getByTypeStream(type: TransactionType): Flow<List<Transaction>>
    fun getByIdStream(id: Int): Flow<Transaction?>
    suspend fun insert(transaction: Transaction)
    suspend fun update(transaction: Transaction)
    suspend fun delete(transaction: Transaction)
}

//Room ashigladag heregjuulelt buh ajliig DAO ruu damjuulna
class OfflineTransactionRepository(
    private val dao: TransactionDao
) : TransactionRepository {
    override fun getAllStream() = dao.getAll()
    override fun getByTypeStream(type: TransactionType) = dao.getByType(type)
    override fun getByIdStream(id: Int) = dao.getById(id)

    override suspend fun insert(transaction: Transaction) = dao.insert(transaction)
    override suspend fun update(transaction: Transaction) = dao.update(transaction)
    override suspend fun delete(transaction: Transaction) = dao.delete(transaction)
}

