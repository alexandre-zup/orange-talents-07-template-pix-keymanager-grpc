package dev.alexandrevieira.manager.apiclients.bcb.dto

import dev.alexandrevieira.manager.data.model.enums.TipoChave

enum class KeyType {
    CPF {
        override fun convert(): TipoChave {
            return TipoChave.CPF
        }
    }, CNPJ {
        override fun convert(): TipoChave {
            TODO("Not yet implemented")
        }
    }, PHONE {
        override fun convert(): TipoChave {
            return TipoChave.CELULAR
        }
    }, EMAIL {
        override fun convert(): TipoChave {
            return TipoChave.EMAIL
        }
    }, RANDOM {
        override fun convert(): TipoChave {
            return TipoChave.ALEATORIA
        }
    };

    abstract fun convert(): TipoChave
}