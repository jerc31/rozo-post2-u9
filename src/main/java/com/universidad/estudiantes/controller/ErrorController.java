package com.universidad.estudiantes.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ErrorController {

    @RequestMapping("/error/403")
    public String accesoDenegado() {
        return "error/403";
    }
}
