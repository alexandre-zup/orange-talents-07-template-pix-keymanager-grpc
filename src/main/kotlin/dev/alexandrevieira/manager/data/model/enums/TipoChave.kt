package dev.alexandrevieira.manager.data.model.enums

import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator

enum class TipoChave {
    CPF {
        override fun valida(chave: String?): Boolean {
            if (chave.isNullOrBlank()) return false

            if (!chave.matches("^[0-9]{11}\$".toRegex())) return false

            CPFValidator().run {
                initialize(null)
                return isValid(chave, null)
            }
        }
    },
    CELULAR {
        override fun valida(chave: String?): Boolean {
            if (chave.isNullOrBlank()) return false

            return chave.matches("^\\+[1-9][0-9]\\d{1,14}$".toRegex())
        }
    },
    EMAIL {
        override fun valida(chave: String?): Boolean {
            if (chave.isNullOrBlank()) return false

            return chave.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}\$".toRegex())
        }
    },
    ALEATORIA {
        override fun valida(chave: String?): Boolean {
            return chave.isNullOrBlank()
        }
    };

    abstract fun valida(chave: String?): Boolean
}