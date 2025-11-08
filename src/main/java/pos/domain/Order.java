package pos.domain;

import java.time.Instant;
import java.util.List;

public class Order {
  public enum Status { PENDIENTE, PREPARANDO, LISTO, ENTREGADO, CERRADO }
  private Long id;
  private Long tableId; // null si es a domicilio/recogida
  private String customerUsername; // para cliente logeado
  private boolean delivery;
  private String deliveryAddress;
  private String deliveryPhone;
  private List<OrderItem> items;
  private Order.Status status;
  private Instant createdAt;

  public Order(Long id, Long tableId, String customerUsername, boolean delivery, String deliveryAddress, String deliveryPhone,
               List<OrderItem> items, Status status, Instant createdAt) {
    this.id = id; this.tableId = tableId; this.customerUsername = customerUsername;
    this.delivery = delivery; this.deliveryAddress = deliveryAddress; this.deliveryPhone = deliveryPhone;
    this.items = items; this.status = status; this.createdAt = createdAt;
  }
  public Long getId(){return id;}
  public Long getTableId(){return tableId;}
  public String getCustomerUsername(){return customerUsername;}
  public boolean isDelivery(){return delivery;}
  public String getDeliveryAddress(){return deliveryAddress;}
  public String getDeliveryPhone(){return deliveryPhone;}
  public List<OrderItem> getItems(){return items;}
  public Status getStatus(){return status;}
  public void setStatus(Status s){this.status=s;}
  public Instant getCreatedAt(){return createdAt;}
  public double total(){ return items.stream().mapToDouble(i -> i.unitPrice()*i.qty()).sum(); }
}
