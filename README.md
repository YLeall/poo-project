# Aplicação Web Java 🚀

## Visão Geral
Este é um projeto de aplicação web Java para gerenciamento de produtos, com injeção de dependência personalizada e roteamento dinâmico.

## Recursos Principais
- Injeção de dependência baseada em anotações personalizadas
- Manipulação de rotas dinâmicas
- Gerenciamento de Singleton
- Abstração de repositório
- Operações CRUD para produtos

## Tecnologias Utilizadas
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Servlets](https://img.shields.io/badge/Servlets-007396?style=for-the-badge)
![HSQLDB](https://img.shields.io/badge/HSQLDB-4479A1?style=for-the-badge)

## Anotações Personalizadas 🏷️

### `@Singleton`
- Marca classes que devem ter apenas uma instância durante a execução do programa
- Aplicável a classes
- Retenção em tempo de execução

### `@Inject`
- Indica injeção automática de dependência para um campo
- Aplicável a atributos
- Retenção em tempo de execução

### `@RepositoryType`
- Especifica a implementação concreta para interfaces de repositório
- Requer definição da classe de implementação
- Usado com `@Inject` para resolver dependências de interface

### `@Rota`
- Define rotas para métodos servlet ou classes
- Aplicável a métodos ou classes
- Mapeia URLs para controladores específicos

## Padrões de Projeto 🧩

### Método Factory
- Cria instâncias de repositórios
- Permite troca fácil entre repositórios de memória e banco de dados

### Gerenciamento Singleton
Duas abordagens de implementação:
1. Anotação `@Singleton`
2. `SingletonManager` para criação de instância com segurança de thread

## Fluxo da Aplicação 🔄
* Ao iniciar, `DatabaseInitializationListener` cria banco de dados
* `InicializadorListener` inicializa singletons e rotas
* `RouteHandler` mapeia URLs para servlets
* `DependencyInjector` injeta dependências automaticamente
* Servlets processam requisições CRUD para produtos

## Componentes Principais 📦
- Modelo: `Produto`
- Repositórios: 
  - `ProdutoRepository` (interface)
  - `MemoriaProdutoRepository`
  - `HSQLProdutoRepository`
- Serviço: `ProdutoService`
- Controladores: Servlets para operações CRUD

## Recursos Dinâmicos 🌟

### Carregamento Dinâmico de Rotas
- Escaneia o pacote `br.com.ucsal`
- Identifica classes anotadas com `@Rota`
- Mapeia automaticamente rotas para instâncias de comando

### Injeção Dinâmica de Dependências
- Descobre campos anotados com `@Inject`
- Instancia e injeta dependências automaticamente
- Resolve implementações de interface usando `@RepositoryType`

## Configuração e Instalação 🛠️

### Pré-requisitos
- Java 8+
- Maven ou Gradle

### Passos
1. Clone o repositório
2. Instale as dependências
3. Execute a aplicação

```bash
git clone https://github.com/seuusuario/nome-do-repositorio.git](https://github.com/YLeall/poo-project.git)
cd poo-project
mvn clean install
mvn tomcat:run  # ou comando equivalente
```
