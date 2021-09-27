package dev.alexandrevieira.manager.apiclients.bcb.dto

import dev.alexandrevieira.manager.data.model.enums.TipoConta

enum class AccountType {
    CACC {
        override fun convert(): TipoConta {
            return TipoConta.CONTA_CORRENTE
        }
    },
    SVGS {
        override fun convert(): TipoConta {
            return TipoConta.CONTA_POUPANCA
        }
    };

    abstract fun convert(): TipoConta
}