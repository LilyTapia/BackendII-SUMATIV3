# HATEOAS API Testing Guide with Postman

## ✅ Issue Fixed!

The problem with null/empty relationships has been resolved by:

1. **Enhanced PedidoService** - Now properly resolves cliente and productos relationships from IDs
2. **Enhanced ReservaService** - Now properly resolves cliente and producto relationships from IDs  
3. **Added DTO Classes** - PedidoRequest and ReservaRequest for easier API consumption
4. **New Endpoints** - Simplified endpoints that accept just IDs instead of full objects

## 🚀 How to Test with Postman

### 1. Start the Application
```bash
mvn spring-boot:run
```

### 2. Authentication (Required)
First, you need to authenticate to get a JWT token. The API uses Spring Security.

**POST** `http://localhost:8080/api/auth/login`
```json
{
  "email": "user@example.com",
  "password": "password"
}
```

Use the returned JWT token in the `Authorization` header for all subsequent requests:
```
Authorization: Bearer <your-jwt-token>
```

### 3. Create Test Data

#### Create a Category
**POST** `http://localhost:8080/api/categorias`
```json
{
  "nombre": "Papelería",
  "descripcion": "Productos de papelería y oficina"
}
```

#### Create a Product
**POST** `http://localhost:8080/api/productos`
```json
{
  "nombre": "Cuaderno Universitario",
  "descripcion": "Cuaderno de 100 hojas rayadas",
  "precio": 5.99,
  "stock": 50,
  "categoria": {
    "id": 1
  }
}
```

#### Create a Client
**POST** `http://localhost:8080/api/clientes`
```json
{
  "nombre": "Juan",
  "apellido": "Pérez",
  "email": "juan.perez@email.com",
  "contraseña": "password123",
  "puntosFidelidad": 0
}
```

### 4. Test HATEOAS Responses

#### Get All Products (with HATEOAS links)
**GET** `http://localhost:8080/api/productos`

**Expected Response:**
```json
{
  "_embedded": {
    "productos": [
      {
        "id": 1,
        "nombre": "Cuaderno Universitario",
        "descripcion": "Cuaderno de 100 hojas rayadas",
        "precio": 5.99,
        "stock": 50,
        "categoria": {
          "id": 1,
          "nombre": "Papelería",
          "descripcion": "Productos de papelería y oficina"
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
    ]
  },
  "_links": {
    "self": {
      "href": "http://localhost:8080/api/productos"
    }
  }
}
```

### 5. Create Orders with Proper Relationships

#### ✅ NEW: Create Order with IDs (RECOMMENDED)
**POST** `http://localhost:8080/api/pedidos/crear`
```json
{
  "clienteId": 1,
  "productosIds": [1],
  "estado": "PENDIENTE"
}
```

#### ✅ NEW: Create Reservation with IDs (RECOMMENDED)  
**POST** `http://localhost:8080/api/reservas/crear`
```json
{
  "clienteId": 1,
  "productoId": 1,
  "cantidad": 2,
  "estado": "PENDIENTE"
}
```

### 6. Navigate Using HATEOAS Links

#### Get Order with Full Relationships
**GET** `http://localhost:8080/api/pedidos/1`

**Expected Response:**
```json
{
  "id": 1,
  "fecha": "2025-07-13T16:20:00",
  "estado": "PENDIENTE",
  "cliente": {
    "id": 1,
    "nombre": "Juan",
    "apellido": "Pérez",
    "email": "juan.perez@email.com"
  },
  "listaProductos": [
    {
      "id": 1,
      "nombre": "Cuaderno Universitario",
      "precio": 5.99,
      "categoria": {
        "id": 1,
        "nombre": "Papelería"
      }
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
    "update": {
      "href": "http://localhost:8080/api/pedidos/1"
    },
    "delete": {
      "href": "http://localhost:8080/api/pedidos/1"
    }
  }
}
```

### 7. Test Navigation Between Resources

1. **Get Client** → Follow `pedidos` link to see client's orders
2. **Get Order** → Follow `cliente` link to see order's client
3. **Get Order** → Follow `productos` link to see all products
4. **Get Product** → Follow `categoria` link to see product's category

## 🔧 Troubleshooting

### If you still get null relationships:
1. Make sure you're using the new `/crear` endpoints
2. Verify the IDs exist in the database
3. Check that you're sending valid JSON with correct field names
4. Ensure authentication token is valid

### Common Issues:
- **401 Unauthorized**: Add JWT token to Authorization header
- **400 Bad Request**: Check JSON format and required fields
- **404 Not Found**: Verify the resource IDs exist

## 📋 Available Endpoints

### Products
- `GET /api/productos` - List all products with HATEOAS
- `GET /api/productos/{id}` - Get product with category link
- `POST /api/productos` - Create product

### Clients  
- `GET /api/clientes` - List all clients with HATEOAS
- `GET /api/clientes/{id}` - Get client with orders link
- `POST /api/clientes` - Create client

### Orders
- `GET /api/pedidos` - List all orders with HATEOAS
- `GET /api/pedidos/{id}` - Get order with client/products links
- `POST /api/pedidos/crear` - ✅ **NEW: Create order with IDs**

### Reservations
- `GET /api/reservas` - List all reservations with HATEOAS  
- `GET /api/reservas/{id}` - Get reservation with client/product links
- `POST /api/reservas/crear` - ✅ **NEW: Create reservation with IDs**

### Categories
- `GET /api/categorias` - List all categories with HATEOAS
- `GET /api/categorias/{id}` - Get category with products link
- `POST /api/categorias` - Create category

The HATEOAS implementation is now fully functional with proper relationship resolution!
