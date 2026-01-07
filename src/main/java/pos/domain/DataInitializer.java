package pos.domain;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pos.repository.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Configuration("domainDataInitializer")
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(
            UserRepository userRepository,
            CategoryRepository categoryRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository,
            SaleRepository saleRepository) {
        return args -> {
            try {
                System.out.println("\n========== INICIALIZANDO DATOS (CAT/PROD/VENTAS) ==========\n");

                // No depende del conteo de usuarios; se asume que YA existen.
                long userCount = userRepository.count();
                if (userCount == 0) {
                    System.out.println("⚠ No hay usuarios. No se crearan pedidos/ventas sin usuarios.");
                    return;
                }

                // 1) Categorias
                if (categoryRepository.count() == 0) {
                    categoryRepository.save(Category.builder().nombre("Bebidas Frias").descripcion("Refrescos, jugos, sodas").build());
                    categoryRepository.save(Category.builder().nombre("Bebidas Calientes").descripcion("Cafe, te, chocolates").build());
                    categoryRepository.save(Category.builder().nombre("Platos Principales").descripcion("Carnes, pescados, pastas").build());
                    categoryRepository.save(Category.builder().nombre("Ensaladas").descripcion("Ensaladas frescas").build());
                    categoryRepository.save(Category.builder().nombre("Pizzas").descripcion("Pizzas italianas").build());
                    categoryRepository.save(Category.builder().nombre("Postres").descripcion("Postres y helados").build());
                    categoryRepository.save(Category.builder().nombre("Aperitivos").descripcion("Tapas y entrada").build());
                    categoryRepository.save(Category.builder().nombre("Hamburguesas").descripcion("Burgers y sandwiches").build());
                    System.out.println("✓ Categorias creadas");
                } else {
                    System.out.println("✓ Categorias ya existen");
                }
                List<Category> categories = categoryRepository.findAll();

                // 2) Productos
                if (productRepository.count() == 0) {
                    // Categorias por indice (mismo orden que arriba)
                    Category bebidasFrias = categories.get(0);
                    Category bebidasCalientes = categories.get(1);
                    Category platosPrincipales = categories.get(2);
                    Category ensaladas = categories.get(3);
                    Category pizzas = categories.get(4);
                    Category postres = categories.get(5);
                    Category aperitivos = categories.get(6);
                    Category hamburguesas = categories.get(7);

                    List<Product> seed = new ArrayList<>();
                    // Bebidas Frias
                    seed.add(Product.builder().name("Coca Cola 330ml").category(bebidasFrias).price(new BigDecimal("2.50")).stock(100).active(true).build());
                    seed.add(Product.builder().name("Agua Mineral").category(bebidasFrias).price(new BigDecimal("1.50")).stock(200).active(true).build());
                    seed.add(Product.builder().name("Jugo Natural").category(bebidasFrias).price(new BigDecimal("3.75")).stock(50).active(true).build());
                    seed.add(Product.builder().name("Cerveza 330ml").category(bebidasFrias).price(new BigDecimal("4.50")).stock(80).active(true).build());

                    // Bebidas Calientes
                    seed.add(Product.builder().name("Cafe Americano").category(bebidasCalientes).price(new BigDecimal("2.50")).stock(150).active(true).build());
                    seed.add(Product.builder().name("Capuchino").category(bebidasCalientes).price(new BigDecimal("3.50")).stock(100).active(true).build());
                    seed.add(Product.builder().name("Chocolate Caliente").category(bebidasCalientes).price(new BigDecimal("3.00")).stock(80).active(true).build());

                    // Platos Principales
                    seed.add(Product.builder().name("Filete de Res").category(platosPrincipales).price(new BigDecimal("18.50")).stock(30).active(true).build());
                    seed.add(Product.builder().name("Pechuga de Pollo").category(platosPrincipales).price(new BigDecimal("12.50")).stock(40).active(true).build());
                    seed.add(Product.builder().name("Pasta Carbonara").category(platosPrincipales).price(new BigDecimal("11.00")).stock(50).active(true).build());
                    seed.add(Product.builder().name("Salmon a la Mantequilla").category(platosPrincipales).price(new BigDecimal("16.75")).stock(25).active(true).build());

                    // Ensaladas
                    seed.add(Product.builder().name("Ensalada Cesar").category(ensaladas).price(new BigDecimal("8.50")).stock(80).active(true).build());
                    seed.add(Product.builder().name("Ensalada Griega").category(ensaladas).price(new BigDecimal("9.00")).stock(75).active(true).build());

                    // Pizzas
                    seed.add(Product.builder().name("Pizza Margherita").category(pizzas).price(new BigDecimal("10.00")).stock(40).active(true).build());
                    seed.add(Product.builder().name("Pizza Pepperoni").category(pizzas).price(new BigDecimal("11.50")).stock(35).active(true).build());
                    seed.add(Product.builder().name("Pizza Cuatro Quesos").category(pizzas).price(new BigDecimal("13.00")).stock(30).active(true).build());

                    // Postres
                    seed.add(Product.builder().name("Cheesecake").category(postres).price(new BigDecimal("5.50")).stock(40).active(true).build());
                    seed.add(Product.builder().name("Brownie").category(postres).price(new BigDecimal("4.00")).stock(60).active(true).build());
                    seed.add(Product.builder().name("Tiramisu").category(postres).price(new BigDecimal("6.00")).stock(50).active(true).build());

                    // Aperitivos
                    seed.add(Product.builder().name("Alitas BBQ").category(aperitivos).price(new BigDecimal("7.50")).stock(70).active(true).build());
                    seed.add(Product.builder().name("Bruschettas Variadas").category(aperitivos).price(new BigDecimal("6.00")).stock(100).active(true).build());

                    // Hamburguesas
                    seed.add(Product.builder().name("Hamburguesa Clasica").category(hamburguesas).price(new BigDecimal("9.50")).stock(60).active(true).build());
                    seed.add(Product.builder().name("Hamburguesa Doble Premium").category(hamburguesas).price(new BigDecimal("13.50")).stock(40).active(true).build());
                    seed.add(Product.builder().name("Sandwich de Atun").category(hamburguesas).price(new BigDecimal("8.00")).stock(50).active(true).build());

                    productRepository.saveAll(seed);
                    System.out.println("✓ Productos creados: " + seed.size());
                } else {
                    System.out.println("✓ Productos ya existen");
                }
                List<Product> products = productRepository.findAll();

                // 3) Pedidos/ventas historicas PAGADAS (solo si no hay ventas aun)
                if (saleRepository.count() == 0 && orderRepository.count() == 0) {
                    User customer = userRepository.findAll().stream()
                            .filter(u -> u.getRole() == Role.CLIENTE)
                            .findFirst()
                            .orElseGet(() -> userRepository.findAll().get(0));

                    User cashier = userRepository.findAll().stream()
                            .filter(u -> u.getRole() == Role.CAJERO)
                            .findFirst()
                            .orElseGet(() -> userRepository.findAll().get(0));

                    Random random = new Random(42);
                    OffsetDateTime now = OffsetDateTime.now();
                    BigDecimal totalVendido = BigDecimal.ZERO;

                    int ordersToCreate = 40;
                    int horizonDays = 120;

                    for (int i = 0; i < ordersToCreate; i++) {
                        int daysAgo = random.nextInt(horizonDays) + 1;
                        OffsetDateTime orderDate = now.minusDays(daysAgo)
                                .withHour(9 + random.nextInt(12))
                                .withMinute(random.nextInt(60))
                                .withSecond(0)
                                .withNano(0);

                        Order order = Order.builder()
                                .orderDate(orderDate)
                                .createdAt(orderDate)
                                .updatedAt(orderDate.plusMinutes(10))
                                .status(OrderStatus.PAGADO)
                                .customer(customer)
                                .serviceSession(null)
                                .address(null)
                                .note("Pedido historico " + (i + 1))
                                .total(BigDecimal.ZERO)
                                .items(new ArrayList<>())
                                .build();

                        int itemsCount = 1 + random.nextInt(4);
                        BigDecimal orderTotal = BigDecimal.ZERO;
                        for (int j = 0; j < itemsCount; j++) {
                            Product product = products.get(random.nextInt(products.size()));
                            int qty = 1 + random.nextInt(3);

                            OrderItem item = OrderItem.builder()
                                    .order(order)
                                    .product(product)
                                    .productName(product.getName())
                                    .unitPrice(product.getPrice())
                                    .quantity(qty)
                                    .comment(null)
                                    .build();
                            order.getItems().add(item);

                            orderTotal = orderTotal.add(product.getPrice().multiply(BigDecimal.valueOf(qty)));
                        }

                        order.setTotal(orderTotal);
                        Order savedOrder = orderRepository.save(order);

                        saleRepository.save(Sale.builder()
                                .date(orderDate.plusMinutes(5 + random.nextInt(25)))
                                .total(orderTotal)
                                .user(cashier)
                                .order(savedOrder)
                                .build());

                        totalVendido = totalVendido.add(orderTotal);
                    }

                    System.out.println("✓ Pedidos PAGADOS creados: " + ordersToCreate);
                    System.out.println("✓ Total vendido: " + totalVendido);
                } else {
                    System.out.println("✓ Ya existen pedidos/ventas; no se duplican");
                }

                System.out.println("\n========== INICIALIZACION COMPLETADA ==========\n");
            } catch (Exception e) {
                System.err.println("ERROR inicializando datos: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
}
