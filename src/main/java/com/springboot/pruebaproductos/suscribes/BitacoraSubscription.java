package com.springboot.pruebaproductos.suscribes;

public class BitacoraSubscription {
    private String id;
    private String accion;
    private String body;
    private String timestamp;

    public BitacoraSubscription(String id, String accion, String body, String timestamp) {
        this.id = id;
        this.accion = accion;
        this.body = body;
        this.timestamp = timestamp;
    }

    public BitacoraSubscription() {
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccion() {
        return this.accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public String getBody() {
        return this.body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
