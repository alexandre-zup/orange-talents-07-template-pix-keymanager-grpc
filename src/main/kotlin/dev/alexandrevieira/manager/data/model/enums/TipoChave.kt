package dev.alexandrevieira.manager.data.model.enums

import dev.alexandrevieira.manager.apiclients.bcb.dto.KeyType
import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator

enum class TipoChave {
    CPF {
        override fun valida(chave: String?): Boolean {
            if (chave.isNullOrBlank())
                return false

            if (!chave.matches("^[0-9]{11}\$".toRegex()))
                return false

            CPFValidator().run {
                initialize(null)
                return isValid(chave, null)
            }
        }

        override fun converte(): KeyType {
            return KeyType.CPF
        }
    },
    CELULAR {
        override fun valida(chave: String?): Boolean {
            if (chave.isNullOrBlank())
                return false

            return chave.matches("^\\+[1-9][0-9]\\d{1,14}$".toRegex())
        }

        override fun converte(): KeyType {
            return KeyType.PHONE
        }
    },
    EMAIL {
        override fun valida(chave: String?): Boolean {
            if (chave.isNullOrBlank())
                return false

            return chave.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}\$".toRegex())
        }

        override fun converte(): KeyType {
            return KeyType.EMAIL
        }
    },
    ALEATORIA {
        override fun valida(chave: String?): Boolean {
            return chave.isNullOrBlank()
        }

        override fun converte(): KeyType {
            return KeyType.RANDOM
        }
    };

    abstract fun valida(chave: String?): Boolean
    abstract fun converte(): KeyType
}