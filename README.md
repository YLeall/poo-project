Adorei esses diagramas! Vou adicioná-los ao README para deixar a documentação ainda mais completa.

# Aplicação Web Java 🚀

## Visão Geral
Este é um projeto de aplicação web Java para gerenciamento de produtos, com injeção de dependência personalizada e roteamento dinâmico.

## Diagrama de Classe
![Diagrama de Classes](https://github.com/user-attachments/assets/a778c424-5f42-47db-8735-fba15e296c57)

## Diagrama de Sequência
![Diagrama de Sequência](https://github.com/user-attachments/assets/32673848-a0a4-4c94-b4bd-75ea1dfc2f54)

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

Exemplo de implementação:
```java
public ProdutoRepository<Produto, Integer> getMemoriaProdutoRepository() {
    return SingletonManager.getInstance(MemoriaProdutoRepository.class);
}
public ProdutoRepository<Produto, Integer> getHSQLProdutoRepository() {
    return new HSQLProdutoRepository();
}
```

### Gerenciamento Singleton
Duas abordagens de implementação:
1. Anotação `@Singleton`
2. `SingletonManager` para criação de instância com segurança de thread

Código-chave do Singleton:
```java
public static <T> T getInstance(Class<T> clazz) {
    if (instance == null) {
        synchronized (SingletonManager.class) {
            if (instance == null) {
                instance = constructor.newInstance();
                singletonInstances.put(clazz, instance);
            }
        }
    }
    return instance;
}
```

## Fluxo da Aplicação 🔄
* Ao iniciar, `DatabaseInitializationListener` cria banco de dados
* `InicializadorListener` inicializa singletons e rotas
* `RouteHandler` mapeia URLs para servlets
* `DependencyInjector` injeta dependências automaticamente
* Servlets processam requisições CRUD para produtos

## Recursos Dinâmicos 🌟

### Carregamento Dinâmico de Rotas
Código-chave para carregamento de rotas:
```java
Reflections reflections = new Reflections("br.com.ucsal");
Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Rota.class);
```

### Injeção Dinâmica de Dependências
Trecho representativo de injeção:
```java
for (Class<?> clazz : allClasses) {
    if (hasInjectFields(clazz)) {
        Object instance = createInstance(clazz);
        injectFieldDependencies(instance);
    }
}
```

## Componentes Principais 📦
- Modelo: `Produto`
- Repositórios: 
  - `ProdutoRepository` (interface)
  - `MemoriaProdutoRepository`
  - `HSQLProdutoRepository`
- Serviço: `ProdutoService`
- Controladores: Servlets para operações CRUD

## Configuração e Instalação 🛠️

### Pré-requisitos
- Java 8+
- Maven ou Gradle

### Passos
1. Clone o repositório
2. Instale as dependências
3. Execute a aplicação
```bash
git clone https://github.com/YLeall/poo-project.git
cd nome-do-repositorio
mvn clean install
mvn tomcat:run  # ou comando equivalente
```

