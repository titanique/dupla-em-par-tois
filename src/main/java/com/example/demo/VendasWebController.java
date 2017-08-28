package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
	public class VendasWebController {
		private ProdutoRepositorioV2 pr;
		private VendaRepositorioV2 vr;
		


	    @Autowired
	    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
	        auth.inMemoryAuthentication().withUser("user").password("password").roles("USER");
	    }


		@Autowired
		public VendasWebController(ProdutoRepositorioV2 pr, VendaRepositorioV2 vr) {
			this.pr = pr;
			this.vr = vr;
		}
		
		@GetMapping("/produtosweb")
		public String consultarProdutos(Model model) {
			Iterable<Produto> prod = pr.findAll();
			model.addAttribute("prod", prod);
			return "produtosweb";
		}
			
		@GetMapping("/vendasweb")
		public Iterable<Venda> consultarVendas() {
			return vr.findAll();
		}
		
		@GetMapping("/vendasweb{id}")
		public ResponseEntity<Venda> consultarVenda(@PathVariable("id") int id) {
			Venda v = vr.findOne(id);
			if(v != null) {
				return new ResponseEntity<>(v, HttpStatus.OK);
			} else {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
		}
		
		@PostMapping("/vendasweb")
		public ResponseEntity<Void> adicionarVenda(@RequestBody Venda venda, UriComponentsBuilder uc) {
			if(venda.getId()!=0 && vr.exists(venda.getId())) {
				return new ResponseEntity<Void>(HttpStatus.CONFLICT);
			}
			Venda novaVenda = vr.save(venda);
			HttpHeaders cabecalho = new HttpHeaders();
			cabecalho.setLocation(uc.path("/venads/{id}").buildAndExpand(novaVenda.getId()).toUri());
			return new ResponseEntity<Void>(cabecalho, HttpStatus.CREATED);
		}

		@PutMapping("/vendasweb{codVenda}/{codProduto}/{quant}")
		public ResponseEntity<Venda> adicionarProdutoVenda(@PathVariable("codVenda") int codVenda, @PathVariable("codProduto") int codProduto, @PathVariable("quant") int quantidade) {
			Venda v = vr.findOne(codVenda);
			if(v != null) {
				Produto p = pr.findOne(codProduto);
				if (p != null) {
					v.vender(p, quantidade);
					vr.save(v);
					return new ResponseEntity<Venda>(v, HttpStatus.OK);
				}
			}
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}
		
	}
	

