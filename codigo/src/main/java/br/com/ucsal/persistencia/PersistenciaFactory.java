package br.com.ucsal.persistencia;

public class PersistenciaFactory {

	public static final int MEMORIA = 0;
	public static final int HSQL = 1;
	
	public static ProdutoRepository<?, ?> getProdutoRepository(int type) {
		ProdutoRepository<?, ?> produtoRepository;
		switch (type) {
		case 0: {
			produtoRepository = MemoriaProdutoRepository.getInstancia();
			break;
		}
		case 1: {
			produtoRepository = new HSQLProdutoRepository();

			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + type);
		}
		return produtoRepository;
	}
	
	
}
