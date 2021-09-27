package dev.alexandrevieira.manager.data.model.enums

import dev.alexandrevieira.manager.apiclients.bcb.dto.AccountType

enum class TipoConta {
    CONTA_CORRENTE {
        override fun converte(): AccountType {
            return AccountType.CACC
        }
    }, CONTA_POUPANCA {
        override fun converte(): AccountType {
            return AccountType.SVGS
        }
    };

    abstract fun converte() : AccountType
}