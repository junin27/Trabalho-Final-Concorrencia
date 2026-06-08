# Banco Digital Concorrente: Trabalho Prático

Este projeto foi desenvolvido para a disciplina de **Desenvolvimento de Software para Concorrência** da **Universidade de Rio Verde (UniRV)**. 
O objetivo principal é explorar de forma incremental e detalhada os conceitos de concorrência na linguagem Java, partindo de uma abordagem ingênua e evoluindo para mecanismos robustos de sincronização.

---

## 🎯 Objetivo Geral

Projetar, implementar e analisar o comportamento de um sistema concorrente de **Banco Digital** focado em transações simultâneas de transferência entre contas, validando a integridade dos saldos (conservação da invariante monetária).

- **Entrega 01**: Implementação ingênua (sem sincronização) demonstrando condições de corrida e atualizações perdidas (*Lost Update*).
- **Entrega 02**: Refatoração aplicando mecanismos de exclusão mútua, semáforos, locks e pools de threads com teste de estresse.

---

## 🛠️ Tecnologias Utilizadas

- **Linguagem**: Java 8 ou superior (compatível com a JRE de desenvolvimento local).
- **Gerenciador de Dependências**: Maven.
- **Suíte de Testes**: JUnit 5 (Jupiter).
- **Integração Contínua (CI)**: GitHub Actions.
- **Documentação**: LaTeX.

---

## 🏗️ Arquitetura do Projeto

O código está organizado seguindo alta coesão e princípios do SOLID:

```text
.
├── pom.xml                                   # Arquivo de build e dependências Maven
├── README.md                                 # Documentação do projeto
├── Relatorios/                               # Relatórios técnicos e logs de simulação
│   ├── entrega01.tex                         # Relatório em LaTeX
│   └── log_simulacao.txt                     # Log consolidado da execução concorrente
└── src/
    ├── main/java/br/com/bancodigital/
    │   ├── config/BankConfig.java            # Constantes da simulação concorrente
    │   ├── domain/Account.java               # Modelo de dados de conta bancária
    │   ├── logging/CustomLogFormatter.java   # Formatador estruturado de logs com threads
    │   ├── service/                          # Lógica de serviços de transferência
    │   │   ├── TransferService.java          # Interface desacoplada
    │   │   └── NaiveTransferService.java     # Implementação concorrente ingênua
    │   └── simulation/BankSimulation.java    # Executável para rodar a simulação
    └── test/java/br/com/bancodigital/
        ├── domain/AccountTest.java           # Testes unitários da conta
        └── service/NaiveTransferServiceTest.java # Testes unitários e concorrência ingênua
```

---

## 🚀 Como Instalar e Executar

### Pré-requisitos
- Ter o **JDK 8** (ou superior) instalado e disponível no PATH do sistema.

### 1. Clonar o Repositório
```bash
git clone https://github.com/junin27/Trabalho-Final-Concorrencia.git
cd Trabalho-Final-Concorrencia
```

### 2. Executar via Terminal (Sem dependências externas)
Como o projeto possui uma classe de simulação independente, é possível compilá-lo e executá-lo diretamente com os compiladores padrão do Java:

**Compilação:**
```bash
javac -d target/classes src/main/java/br/com/bancodigital/config/*.java src/main/java/br/com/bancodigital/domain/*.java src/main/java/br/com/bancodigital/logging/*.java src/main/java/br/com/bancodigital/service/*.java src/main/java/br/com/bancodigital/simulation/*.java
```

**Execução da Simulação Concorrente:**
```bash
java -cp target/classes br.com.bancodigital.simulation.BankSimulation
```

---

## 🧪 Suíte de Testes Automatizados (TDD)

Toda a lógica foi desenvolvida baseada em **TDD (Test-Driven Development)** sob os critérios **FIRST**. 

### Executando testes locais via Maven:
Se você possuir o Maven instalado localmente, execute na pasta raiz:
```bash
mvn clean test
```
*Dica*: Os testes unitários também podem ser disparados diretamente a partir de qualquer IDE moderna (IntelliJ IDEA, VS Code com o Java Extension Pack ou Eclipse) abrindo o arquivo `AccountTest.java` ou `NaiveTransferServiceTest.java`.

### CI/CD (GitHub Actions)
A cada push ou pull request nas branches do projeto, o pipeline `.github/workflows/maven.yml` é disparado automaticamente no GitHub Actions, executando toda a suíte de testes em um container Ubuntu isolado com Java 8.

---

## 📐 Boas Práticas e Clean Code Aplicados

- **Funções Curtas**: Todos os métodos têm de 4 a 20 linhas de código lógico.
- **Arquivos Coesos**: Classes focadas com tamanho inferior a 300 linhas de código.
- **Injeção de Dependências**: Lógicas acopladas são injetadas pelos construtores das classes, permitindo fácil substituição e mockagem.
- **Sem Números Mágicos**: Todas as variáveis globais de simulação estão centralizadas em `BankConfig.java`.
- **Tratamento de Erros Explicito**: Lançamento de exceções fortemente tipadas especificando o valor inválido e o comportamento esperado.
- **Logging Estruturado**: Utilização de `java.util.logging.Logger` com formato customizado exibindo a identificação da Thread de origem para depuração concorrente clara.
