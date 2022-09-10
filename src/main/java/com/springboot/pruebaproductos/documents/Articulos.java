package com.springboot.pruebaproductos.documents;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Pattern.Flag;

import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "Articulos")
public class Articulos {
    @Id
    // @NotEmpty
    // @Length(min = 3, max = 30)
    private String clave;
    @NotEmpty
    @Length(max = 30)
    private String nombre;
    @NotNull
    private Float precio;
    @NotNull
    @Pattern(regexp = "Pieza|Kilogramo|Pulgada|Litro", flags = Flag.CASE_INSENSITIVE)
    private String um;

    public Articulos(String clave, String nombre, Float precio, String um) {
        super();
        this.clave = clave;
        this.nombre = nombre;
        this.precio = precio;
        this.um = um;
    }

    public Articulos() {
    }

    public String getClave() {
        return this.clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getNombre() {
        return this.nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Float getPrecio() {
        return this.precio;
    }

    public void setPrecio(Float precio) {
        this.precio = precio;
    }

    public String getUm() {
        return this.um;
    }

    public void setUm(String um) {
        this.um = um;
    }
}
