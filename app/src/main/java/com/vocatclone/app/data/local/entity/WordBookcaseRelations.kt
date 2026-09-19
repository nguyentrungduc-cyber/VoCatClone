package com.vocatclone.app.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/**
 * Một Word kèm danh sách tất cả các Bookcase mà nó thuộc về.
 */
data class WordWithBookcases(
    @Embedded val word: WordEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = WordBookcaseCrossRef::class,
            parentColumn = "wordId",
            entityColumn = "bookcaseId"
        )
    )
    val bookcases: List<BookcaseEntity>
)

/**
 * Một Bookcase kèm danh sách tất cả các Word thuộc về nó.
 */
data class BookcaseWithWords(
    @Embedded val bookcase: BookcaseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = WordBookcaseCrossRef::class,
            parentColumn = "bookcaseId",
            entityColumn = "wordId"
        )
    )
    val words: List<WordEntity>
)
