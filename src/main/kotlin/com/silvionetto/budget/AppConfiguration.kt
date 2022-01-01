package com.silvionetto.budget

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.core.io.ClassPathResource

@Configuration
class AppConfiguration {

    @Autowired
    lateinit var userService: UserService
    
    @Autowired
    lateinit var categoryService: CategoryService
    
    @Autowired
    lateinit var subCategoryService: SubCategoryService

    @Autowired
    lateinit var storeService: StoreService

    @Autowired
    lateinit var transactionService: TransactionService

    @Bean
    @Order(1)
    fun databaseInitializer() = ApplicationRunner {

        userService.saveUser(User("admin", "admin", "admin"))
        userService.saveUser(User("silvionetto", "Silvio", "Netto"))

        var category = categoryService.saveCategory(BudgetCategory("Renda", BudgetType.INCOME))
        subCategoryService.saveSubCategory(BudgetSubCategory("ING", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Sociale Verzekeringsbank", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Debora", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Restituicao", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Transferencia", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Receita de juros", category))

        category = categoryService.saveCategory(BudgetCategory("Unknown_Income", BudgetType.INCOME))
        subCategoryService.saveSubCategory(BudgetSubCategory("Unknown_Income", category))

        category = categoryService.saveCategory(BudgetCategory("Unknown_Expense", BudgetType.EXPENSE))
        subCategoryService.saveSubCategory(BudgetSubCategory("Unknown_Expense", category))

        category = categoryService.saveCategory(BudgetCategory("Despesas Domesticas", BudgetType.EXPENSE))
        subCategoryService.saveSubCategory(BudgetSubCategory("Hipoteca", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Eletricidade", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Gas", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Agua", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Celular", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Cafe", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("TV a cabo", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Internet", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Moveis", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Eletrodomestico", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Suprimentos", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Manutencao", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Melhorias", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Empregada", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Cloud", category))

        category = categoryService.saveCategory(BudgetCategory("Vida Diaria", BudgetType.EXPENSE))
        subCategoryService.saveSubCategory(BudgetSubCategory("Almoco", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Mercearia", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Roupa", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Calcado", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Perfume", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Maquiagem", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Jantar", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Lavanderia", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Barbeiro", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Lanche", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Cabelo", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Unha", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Cafe da Manha", category))

        category = categoryService.saveCategory(BudgetCategory("Joao Pedro", BudgetType.EXPENSE))
        subCategoryService.saveSubCategory(BudgetSubCategory("Medicamento", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Roupa", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Escola", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Merenda", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Material escolar", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Baba", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Brinquedos", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Esporte", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Livros", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Revistas", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Férias", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Cabelo", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("PSN", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Google", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Mesada", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Bicicleta", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Advogada", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Imigração", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Celular", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Relógio", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Festa", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Passagem", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Pensão", category))

        category = categoryService.saveCategory(BudgetCategory("Gabriel", BudgetType.EXPENSE))
        subCategoryService.saveSubCategory(BudgetSubCategory("Medicamento", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Óculos", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Leite", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Calçado", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Roupa", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Escola", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Merenda", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Material escolar", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Cabelo", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("PSN", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Nitendo", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Baba", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Brinquedos", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Esporte", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Livros", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Revistas", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Fralda", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Carrinho", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Foto", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Parque", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Festa", category))

        category = categoryService.saveCategory(BudgetCategory("Transporte", BudgetType.EXPENSE))
        subCategoryService.saveSubCategory(BudgetSubCategory("Aluguel", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Lease", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Moto", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Gasolina", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Estacionamento", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Manutenção", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Melhorias", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Habilitação", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Registro/Licensa", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Pedagio", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Bicicleta", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Taxi", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Multa", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Onibus", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Trem", category))

        category = categoryService.saveCategory(BudgetCategory("Saude", BudgetType.EXPENSE))
        subCategoryService.saveSubCategory(BudgetSubCategory("Medico", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Medicamento", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Plano de saúde", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Suplemento Alimentar", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Emergencia", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Dentista", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Fisioterapeuta", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Oculos", category))

        category = categoryService.saveCategory(BudgetCategory("Seguro", BudgetType.EXPENSE))
        subCategoryService.saveSubCategory(BudgetSubCategory("Carro", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Vida", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Casa", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Moto", category))

        category = categoryService.saveCategory(BudgetCategory("Educacao", BudgetType.EXPENSE))
        subCategoryService.saveSubCategory(BudgetSubCategory("Ensino", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Cursos", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Livros", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Jiu-Jitsu", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Academia", category))

        category = categoryService.saveCategory(BudgetCategory("Caridade/Presente", BudgetType.EXPENSE))
        subCategoryService.saveSubCategory(BudgetSubCategory("Presentes", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Doacoes", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Donativos Religiosos", category))

        category = categoryService.saveCategory(BudgetCategory("Poupanca", BudgetType.EXPENSE))
        subCategoryService.saveSubCategory(BudgetSubCategory("Fundo de emergencia", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Transferencia para poupança", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Aposentadoria", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Barco", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Joao Pedro", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Gabriel", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Debora", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Casa", category))

        category = categoryService.saveCategory(BudgetCategory("Obrigacoes", BudgetType.EXPENSE))
        subCategoryService.saveSubCategory(BudgetSubCategory("Emprestimo escolar", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Emprestimo", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Cartão de crédito", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Função crédito", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Imposto", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Banco", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Taxas", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Imigração", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Notário", category))

        category = categoryService.saveCategory(BudgetCategory("Despesas de Negocio", BudgetType.EXPENSE))
        subCategoryService.saveSubCategory(BudgetSubCategory("Despesas deduziveis", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Despesas não deduziveis", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Taxi", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Hotel", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Passagem", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Lavanderia", category))

        category = categoryService.saveCategory(BudgetCategory("Diversao", BudgetType.EXPENSE))
        subCategoryService.saveSubCategory(BudgetSubCategory("Filmes", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Musicas", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Jogos", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Cinema", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Livros", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Kite", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Surf", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Skate", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Corrida", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Camera", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Brinquedos", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Bebidas", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Jardinagem", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Parque", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Museum", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Teatro", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Barco", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Festa", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Spa", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Outros", category))

        category = categoryService.saveCategory(BudgetCategory("Animal", BudgetType.EXPENSE))
        subCategoryService.saveSubCategory(BudgetSubCategory("Comida", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Medicamento", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Brinquedos", category))

        category = categoryService.saveCategory(BudgetCategory("Assinatura", BudgetType.EXPENSE))
        subCategoryService.saveSubCategory(BudgetSubCategory("Jornal", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Revista", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Clube", category))

        category = categoryService.saveCategory(BudgetCategory("Ferias", BudgetType.EXPENSE))
        subCategoryService.saveSubCategory(BudgetSubCategory("Viagem", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Acomodação", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Alimento", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Aluguel de carro", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Diversão", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Mala", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Combustivel", category))

        category = categoryService.saveCategory(BudgetCategory("Variados", BudgetType.EXPENSE))
        subCategoryService.saveSubCategory(BudgetSubCategory("Avaliador", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Contador", category))
        subCategoryService.saveSubCategory(BudgetSubCategory("Correio", category))

        loadFiles()
    }

    fun loadFiles() {
        fun getLine(l : String): List<String> {
            if (l.startsWith("\"")) {
                return l.replace(", '",", \"").replace("',", "\",").replace("(","").replace(")", "").split("\", \"")
            }
            return l.replace("(","").replace(")", "").split(", '")
        }

        fun getStoreName(column: String): String {
            if (column.startsWith("\"")) {
                return column.replace("\"","")
            }
            return column.replace("'","")
        }

//        fun getColumns(l : String): Map<String, String> {
//            var line = getLine(l)
//            var columns = LinkedHashMap<String, String>()
//            //columns['storeName'] = getStoreName(l)
//            //columns['transactionSide'] =
//
//        }



        var inputFolder = ClassPathResource("input/store").file
        if (inputFolder.exists()) {
            inputFolder.walk().forEach {
                if (it.isFile) {
                    it.forEachLine { line ->
                        var columns = getLine(line)

                        val storeName = getStoreName(line)
                        val transactionSide = columns[1].replace("'","")
                        val category = columns[2].replace("'","")
                        val subCategoryName = columns[3].replace("'","")
                        storeService.saveStore(storeName, transactionSide, subCategoryName)
                    }
                }
            }
        }

        inputFolder = ClassPathResource("input/extract").file
        if (inputFolder.exists()) {
            inputFolder.walk().forEach {
                if (it.isFile) {
                    var lineNumber: Int = "0".toInt()
                    it.forEachLine { line ->
                        if (lineNumber.compareTo(0) == 0) {
                            lineNumber++
                            return@forEachLine
                        }
                        var columns = line.split("\",\"")
                        if (columns.size == 1) {
                            columns = line.split("\";\"")
                        }

                        val date = columns[0].replace("\"", "")
                        val storeName = columns[1].replace("\"", "")
                        val account = columns[2].replace("\"", "")
                        val contraAccount = columns[3].replace("\"", "")
                        val code = columns[4].replace("\"", "")
                        val transactionSide = columns[5].replace("\"", "")
                        val amount = columns[6].replace("\"", "").replace(",",".")
                        val transactionType = columns[7].replace("\"", "")
                        val notifications = columns[8].replace("\"", "")
                        val store = storeService.saveStore(storeName, transactionSide)
                        transactionService.saveTransaction(Transaction(date.toDate(), store, account, contraAccount, code, transactionSide, amount.toDouble(), transactionType, notifications, store.subCategory))

                        lineNumber++
                    }
                }
            }
        }


    }
}