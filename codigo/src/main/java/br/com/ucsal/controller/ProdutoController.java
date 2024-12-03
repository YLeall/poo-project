package br.com.ucsal.controller;

import br.com.ucsal.annotations.Rota;
import br.com.ucsal.annotations.Inject;
import br.com.ucsal.annotations.RouteNotFoundException;
import br.com.ucsal.service.ProdutoService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/view/*")
public class ProdutoController extends HttpServlet {
    private final RouteHandler routeHandler = RouteHandler.getInstance();
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            routeHandler.handleRequest(request, response);
        } catch (RouteNotFoundException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Página não encontrada");
        } catch (ServletException e) {
            throw e;
        }
    }
}
