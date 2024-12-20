package br.com.ucsal.persistencia;

import br.com.ucsal.annotations.Singleton;
import br.com.ucsal.controller.SingletonManager;
import br.com.ucsal.model.Produto;

public class PersistenciaFactory {

	private static PersistenciaFactory instance;

	private PersistenciaFactory() {
	}

	public static PersistenciaFactory getInstance() {
		return SingletonManager.getInstance(PersistenciaFactory.class);
	}

	public ProdutoRepository<Produto, Integer> getMemoriaProdutoRepository() {
		return SingletonManager.getInstance(MemoriaProdutoRepository.class);
	}

	public ProdutoRepository<Produto, Integer> getHSQLProdutoRepository() {
		return new HSQLProdutoRepository();
	}
}