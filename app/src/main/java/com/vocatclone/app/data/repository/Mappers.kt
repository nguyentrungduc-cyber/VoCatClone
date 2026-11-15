package com.vocatclone.app.data.repository

import com.vocatclone.app.data.local.entity.BookcaseEntity
import com.vocatclone.app.data.local.entity.BookshelfEntity
import com.vocatclone.app.data.local.entity.WordWithMeanings
import com.vocatclone.app.ui.bookcase.BookcaseFormValues
import com.vocatclone.app.ui.bookcase.WordItemUi
import com.vocatclone.app.ui.home.BookcaseUi
import com.vocatclone.app.ui.home.BookshelfUi
import com.vocatclone.app.ui.word.WordDetailUi
import com.vocatclone.app.ui.word.WordFormValues

/** Chuỗi rỗng hoặc toàn khoảng trắng thì lưu là null. */
internal fun String?.nullIfBlank(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

internal fun BookcaseEntity.toUi(wordCount: Int, masteredCount: Int) = BookcaseUi(
    id = id,
    name = name,
    sourceLang = sourceLang,
    targetLang = targetLang,
    wordCount = wordCount,
    masteredCount = masteredCount
)

internal fun BookshelfEntity.toUi(bookcases: List<BookcaseUi>) =
    BookshelfUi(id = id, name = name, bookcases = bookcases)

internal fun BookcaseEntity.toFormValues() = BookcaseFormValues(
    shelfId = bookshelfId,
    name = name,
    sourceLang = sourceLang,
    targetLang = targetLang
)

internal fun WordWithMeanings.toItemUi() = WordItemUi(
    id = word.id,
    term = word.term,
    pronunciation = word.pronunciation,
    partOfSpeech = word.partOfSpeech,
    meanings = sortedMeanings.map { it.meaning },
    description = word.description,
    imageUri = word.imageUri,
    isMastered = word.isMastered,
    createdAt = word.createdAt
)

internal fun WordWithMeanings.toDetailUi() = WordDetailUi(
    id = word.id,
    term = word.term,
    pronunciation = word.pronunciation,
    partOfSpeech = word.partOfSpeech,
    meanings = sortedMeanings.map { it.meaning },
    exampleEn = word.exampleEn,
    exampleVi = word.exampleVi,
    synonym = word.synonym,
    description = word.description,
    imageUri = word.imageUri,
    isMastered = word.isMastered,
    reviewCount = word.reviewCount,
    lastReviewedAt = word.lastReviewedAt,
    createdAt = word.createdAt
)

/** Đổi dữ liệu có sẵn thành giá trị điền sẵn cho form Sửa từ (luôn có ít nhất 1 ô nghĩa). */
internal fun WordWithMeanings.toFormValues() = WordFormValues(
    term = word.term,
    meanings = sortedMeanings.map { it.meaning }.ifEmpty { listOf("") },
    pronunciation = word.pronunciation.orEmpty(),
    partOfSpeech = word.partOfSpeech,
    exampleEn = word.exampleEn.orEmpty(),
    exampleVi = word.exampleVi.orEmpty(),
    synonym = word.synonym.orEmpty(),
    description = word.description.orEmpty(),
    imageUri = word.imageUri
)
