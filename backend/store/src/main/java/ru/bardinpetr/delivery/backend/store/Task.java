package ru.bardinpetr.delivery.backend.store;

public class Task {
    private String id;
    private String address;
    private String addressSignature;

    public Task(String id, String address, String addressSignature) {
        this.id = id;
        this.address = address;
        this.addressSignature = addressSignature;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddressSignature() {
        return addressSignature;
    }

    public void setAddressSignature(String addressSignature) {
        this.addressSignature = addressSignature;
    }
}
