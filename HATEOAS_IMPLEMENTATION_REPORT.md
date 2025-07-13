# HATEOAS Implementation Report

## Overview

This document describes the implementation of HATEOAS (Hypermedia as the Engine of Application State) in the Letras y Papeles backend application. HATEOAS is a constraint of REST application architecture that allows clients to dynamically navigate the API through hypermedia links provided in responses.

## Implementation Details

### 1. Dependencies Added

Added Spring HATEOAS dependency to `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-hateoas</artifactId>
</dependency>
```

### 2. Model Assemblers Created

Created RepresentationModelAssembler classes for each entity to convert domain objects to HATEOAS-enabled representations:

- **ProductoModelAssembler**: Converts Producto entities to EntityModel with links
- **ClienteModelAssembler**: Converts Cliente entities to EntityModel with links
- **PedidoModelAssembler**: Converts Pedido entities to EntityModel with links
- **ReservaModelAssembler**: Converts Reserva entities to EntityModel with links
- **CategoriaModelAssembler**: Converts Categoria entities to EntityModel with links

### 3. Controllers Updated

All REST controllers were updated to return HATEOAS-enabled responses:

#### Return Types Changed:
- `List<Entity>` → `CollectionModel<EntityModel<Entity>>`
- `Entity` → `EntityModel<Entity>`
- `ResponseEntity<Entity>` → `ResponseEntity<EntityModel<Entity>>`

#### Controllers Modified:
- ProductoController
- ClienteController
- PedidoController
- ReservaController
- CategoriaController

### 4. Link Relationships Implemented

#### Producto (Product) Links:
- **self**: Link to the specific product
- **productos**: Link to all products collection
- **categoria**: Link to the product's category
- **update**: Link to update the product
- **delete**: Link to delete the product

#### Cliente (Client) Links:
- **self**: Link to the specific client
- **clientes**: Link to all clients collection
- **pedidos**: Link to client's orders
- **reservas**: Link to client's reservations
- **update**: Link to update the client
- **delete**: Link to delete the client

#### Pedido (Order) Links:
- **self**: Link to the specific order
- **pedidos**: Link to all orders collection
- **cliente**: Link to the order's client
- **productos**: Link to products collection
- **producto-{id}**: Individual links to each product in the order
- **update**: Link to update the order
- **delete**: Link to delete the order

#### Reserva (Reservation) Links:
- **self**: Link to the specific reservation
- **reservas**: Link to all reservations collection
- **cliente**: Link to the reservation's client
- **producto**: Link to the reserved product
- **update**: Link to update the reservation
- **delete**: Link to delete the reservation
- **confirmar**: Link to confirm reservation (if status is PENDIENTE)
- **cancelar**: Link to cancel reservation (if status is PENDIENTE)

#### Categoria (Category) Links:
- **self**: Link to the specific category
- **categorias**: Link to all categories collection
- **productos**: Link to products in this category
- **update**: Link to update the category
- **delete**: Link to delete the category

### 5. Navigation Patterns

The HATEOAS implementation enables the following navigation patterns:

1. **Resource Discovery**: Clients can start from any endpoint and discover related resources
2. **State-based Actions**: Available actions are provided based on resource state
3. **Relationship Navigation**: Easy navigation between related entities
4. **Collection Browsing**: Collections provide links to individual resources

## API Design Benefits

### 1. Self-Documenting API
- Links provide information about available operations
- Reduces need for external documentation
- Makes API more discoverable

### 2. Loose Coupling
- Clients don't need to construct URLs
- Server can change URL structure without breaking clients
- Reduces client-server coupling

### 3. State Management
- Links indicate available actions based on current state
- Prevents invalid operations
- Guides client behavior

### 4. Enhanced Navigation
- Easy traversal between related resources
- Supports complex workflows
- Improves user experience

## Testing

### Unit Tests Updated
All existing unit tests were updated to work with HATEOAS response structure:
- Mock assemblers added to test classes
- Response type assertions updated
- Link verification added where appropriate

### Integration Tests Created
Comprehensive integration tests verify:
- Correct link generation
- Link navigation functionality
- Response structure compliance
- Cross-resource navigation

## Endpoint Examples

### GET /api/productos/1 (Single Product)
```json
{
  "id": 1,
  "nombre": "Cuaderno Universitario",
  "descripcion": "Cuaderno de 100 hojas",
  "precio": 5.99,
  "stock": 50,
  "categoria": {
    "id": 1,
    "nombre": "Papelería"
  },
  "_links": {
    "self": {
      "href": "http://localhost:8080/api/productos/1"
    },
    "productos": {
      "href": "http://localhost:8080/api/productos"
    },
    "categoria": {
      "href": "http://localhost:8080/api/categorias/1"
    },
    "update": {
      "href": "http://localhost:8080/api/productos/1"
    },
    "delete": {
      "href": "http://localhost:8080/api/productos/1"
    }
  }
}
```

### GET /api/productos (Product Collection)
```json
{
  "_embedded": {
    "productoList": [
      {
        "id": 1,
        "nombre": "Cuaderno Universitario",
        "precio": 5.99,
        "_links": {
          "self": {
            "href": "http://localhost:8080/api/productos/1"
          },
          "categoria": {
            "href": "http://localhost:8080/api/categorias/1"
          }
        }
      }
    ]
  },
  "_links": {
    "self": {
      "href": "http://localhost:8080/api/productos"
    }
  }
}
```

### GET /api/clientes/1 (Single Client)
```json
{
  "id": 1,
  "nombre": "Juan",
  "apellido": "Pérez",
  "email": "juan@example.com",
  "puntosFidelidad": 150,
  "_links": {
    "self": {
      "href": "http://localhost:8080/api/clientes/1"
    },
    "clientes": {
      "href": "http://localhost:8080/api/clientes"
    },
    "pedidos": {
      "href": "http://localhost:8080/api/pedidos/cliente/1"
    },
    "reservas": {
      "href": "http://localhost:8080/api/reservas"
    },
    "update": {
      "href": "http://localhost:8080/api/clientes/1"
    },
    "delete": {
      "href": "http://localhost:8080/api/clientes/1"
    }
  }
}
```

### GET /api/pedidos/1 (Single Order)
```json
{
  "id": 1,
  "fecha": "2024-01-15T10:30:00",
  "estado": "CONFIRMADO",
  "cliente": {
    "id": 1,
    "nombre": "Juan",
    "apellido": "Pérez"
  },
  "listaProductos": [
    {
      "id": 1,
      "nombre": "Cuaderno Universitario",
      "precio": 5.99
    },
    {
      "id": 2,
      "nombre": "Bolígrafo Azul",
      "precio": 1.50
    }
  ],
  "_links": {
    "self": {
      "href": "http://localhost:8080/api/pedidos/1"
    },
    "pedidos": {
      "href": "http://localhost:8080/api/pedidos"
    },
    "cliente": {
      "href": "http://localhost:8080/api/clientes/1"
    },
    "productos": {
      "href": "http://localhost:8080/api/productos"
    },
    "producto-1": {
      "href": "http://localhost:8080/api/productos/1"
    },
    "producto-2": {
      "href": "http://localhost:8080/api/productos/2"
    },
    "update": {
      "href": "http://localhost:8080/api/pedidos/1"
    },
    "delete": {
      "href": "http://localhost:8080/api/pedidos/1"
    }
  }
}
```

### GET /api/reservas/1 (Single Reservation - Pending State)
```json
{
  "id": 1,
  "fechaReserva": "2024-01-15T14:30:00",
  "estado": "PENDIENTE",
  "cantidad": 2,
  "cliente": {
    "id": 1,
    "nombre": "Juan",
    "apellido": "Pérez"
  },
  "producto": {
    "id": 1,
    "nombre": "Cuaderno Universitario",
    "precio": 5.99
  },
  "_links": {
    "self": {
      "href": "http://localhost:8080/api/reservas/1"
    },
    "reservas": {
      "href": "http://localhost:8080/api/reservas"
    },
    "cliente": {
      "href": "http://localhost:8080/api/clientes/1"
    },
    "producto": {
      "href": "http://localhost:8080/api/productos/1"
    },
    "update": {
      "href": "http://localhost:8080/api/reservas/1"
    },
    "delete": {
      "href": "http://localhost:8080/api/reservas/1"
    },
    "confirmar": {
      "href": "http://localhost:8080/api/reservas/1/confirmar"
    },
    "cancelar": {
      "href": "http://localhost:8080/api/reservas/1/cancelar"
    }
  }
}
```

## Usage Examples

### 1. Discovering Available Actions
```javascript
// Client receives a reservation and can see available actions
const reservation = await fetch('/api/reservas/1').then(r => r.json());

// Check if confirmation is available
if (reservation._links.confirmar) {
  // Show confirm button to user
  const confirmUrl = reservation._links.confirmar.href;
}

// Check if cancellation is available
if (reservation._links.cancelar) {
  // Show cancel button to user
  const cancelUrl = reservation._links.cancelar.href;
}
```

### 2. Navigating Between Resources
```javascript
// Get a product and navigate to its category
const product = await fetch('/api/productos/1').then(r => r.json());
const categoryUrl = product._links.categoria.href;
const category = await fetch(categoryUrl).then(r => r.json());

// From category, get all products in that category
const productsUrl = category._links.productos.href;
const products = await fetch(productsUrl).then(r => r.json());
```

### 3. Following Client Orders
```javascript
// Get client and navigate to their orders
const client = await fetch('/api/clientes/1').then(r => r.json());
const ordersUrl = client._links.pedidos.href;
const clientOrders = await fetch(ordersUrl).then(r => r.json());

// Navigate to specific order details
const firstOrder = clientOrders._embedded.pedidoList[0];
const orderDetailsUrl = firstOrder._links.self.href;
const orderDetails = await fetch(orderDetailsUrl).then(r => r.json());
```

## Conclusion

The HATEOAS implementation significantly enhances the API's usability and maintainability by:

1. **Providing self-documenting endpoints** that guide client developers
2. **Enabling dynamic navigation** between related resources
3. **Supporting state-based operations** that prevent invalid actions
4. **Reducing coupling** between client and server implementations
5. **Improving discoverability** of API capabilities

The implementation follows REST principles and provides a robust foundation for building sophisticated client applications that can adapt to API changes and provide rich user experiences.```
