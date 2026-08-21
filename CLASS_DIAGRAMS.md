# Class Diagrams

Mermaid class diagrams generated from Java source files in this repository.

## auth-service

```mermaid
classDiagram
  namespace com_vms_auth {
    class AuthServiceApplication
    class AuthServiceApplicationTests
  }
  namespace com_vms_auth_api {
    class ApiError
    <<record>> ApiError
    class ApiExceptionHandler
    class AuthController
    class AuthControllerTest
  }
  namespace com_vms_auth_dto {
    class AccessTokenResponse
    <<record>> AccessTokenResponse
    class AuthResponse
    <<record>> AuthResponse
    class CreateUserRequest
    <<record>> CreateUserRequest
    class ForgotPasswordRequest
    <<record>> ForgotPasswordRequest
    class LoginRequest
    <<record>> LoginRequest
    class RefreshRequest
    <<record>> RefreshRequest
    class ResetPasswordRequest
    <<record>> ResetPasswordRequest
    class SignUpRequest
    <<record>> SignUpRequest
    class UserResponse
    <<record>> UserResponse
  }
  namespace com_vms_auth_entity {
    class PasswordResetToken
    class RefreshToken
    class Role
    <<enumeration>> Role
    class User
  }
  namespace com_vms_auth_repository {
    class PasswordResetTokenRepository
    <<interface>> PasswordResetTokenRepository
    class RefreshTokenRepository
    <<interface>> RefreshTokenRepository
    class UserRepository
    <<interface>> UserRepository
    class AuthRepositoryTest
  }
  namespace com_vms_auth_security {
    class CustomAuthenticationEntryPoint
    class SecurityConfig
  }
  namespace com_vms_auth_security_jwt {
    class JwtAuthenticationFilter
    class JwtService
  }
  namespace com_vms_auth_service {
    class AuthService
    class AuthServiceTest
  }
  namespace com_vms_auth_service_exceptions {
    class AccountInactiveException
    class AuthException
    class EmailAlreadyInUseException
    class InvalidAuthTokenException
    class InvalidCredentialsException
    class InvalidRefreshTokenException
    class InvalidResetTokenException
    class RecordNotFoundException
  }

  PasswordResetTokenRepository --|> JpaRepository
  RefreshTokenRepository --|> JpaRepository
  UserRepository --|> JpaRepository
  CustomAuthenticationEntryPoint ..|> AuthenticationEntryPoint
  JwtAuthenticationFilter --|> OncePerRequestFilter
  AccountInactiveException --|> AuthException
  AuthException --|> RuntimeException
  EmailAlreadyInUseException --|> AuthException
  InvalidAuthTokenException --|> AuthException
  InvalidCredentialsException --|> AuthException
  InvalidRefreshTokenException --|> AuthException
  InvalidResetTokenException --|> AuthException
  RecordNotFoundException --|> AuthException
```

## machine-service

```mermaid
classDiagram
  namespace com_vms_machine {
    class MachineApplication
    class MachineApplicationTests
  }
  namespace com_vms_machine_api {
    class ApiError
    <<record>> ApiError
    class ApiExceptionHandler
    class MachineController
    class MachineInventoryController
    class ProductController
    class MachineControllerTest
    class MachineInventoryControllerTest
  }
  namespace com_vms_machine_dto {
    class AddInventoryRequest
    <<record>> AddInventoryRequest
    class CreateMachineRequest
    <<record>> CreateMachineRequest
    class CreateProductRequest
    <<record>> CreateProductRequest
    class MachineDistanceProjection
    <<interface>> MachineDistanceProjection
    class MachineInventoryResponse
    <<record>> MachineInventoryResponse
    class MachineResponse
    <<record>> MachineResponse
    class NearbyMachinesResponse
    <<record>> NearbyMachinesResponse
    class ProductResponse
    <<record>> ProductResponse
  }
  namespace com_vms_machine_entity {
    class Machine
    class MachineInventory
    class MachineStatus
    <<enumeration>> MachineStatus
    class Product
  }
  namespace com_vms_machine_repository {
    class InventoryRepository
    <<interface>> InventoryRepository
    class MachineRepository
    <<interface>> MachineRepository
    class ProductRepository
    <<interface>> ProductRepository
  }
  namespace com_vms_machine_security {
    class CustomAuthenticationEntryPoint
    class SecurityConfig
  }
  namespace com_vms_machine_security_jwt {
    class JwtAuthenticationFilter
    class JwtService
  }
  namespace com_vms_machine_service {
    class InventoryService
    class MachineService
    class ProductService
    class MachineServiceTest
  }
  namespace com_vms_machine_service_exceptions {
    class InvalidSearchRadiusException
  }

  InventoryRepository --|> JpaRepository
  MachineRepository --|> JpaRepository
  ProductRepository --|> JpaRepository
  CustomAuthenticationEntryPoint ..|> AuthenticationEntryPoint
  JwtAuthenticationFilter --|> OncePerRequestFilter
  InvalidSearchRadiusException --|> RuntimeException
```

## order-service

```mermaid
classDiagram
  namespace com_vms_order {
    class OrderServiceApplication
    class OrderServiceApplicationTests
  }
  namespace com_vms_order_api {
    class ApiError
    <<record>> ApiError
    class ApiExceptionHandler
    class DispenseController
    class OrderController
  }
  namespace com_vms_order_dto {
    class CreateOrderRequest
    <<record>> CreateOrderRequest
    class ItemRequest
    <<record>> ItemRequest
    class DispenseCompletedRequest
    <<record>> DispenseCompletedRequest
    class DispenseRequest
    <<record>> DispenseRequest
    class DispenseResponse
    <<record>> DispenseResponse
    class DispenseItem
    <<record>> DispenseItem
    class ListOrdersResponse
    <<record>> ListOrdersResponse
    class OrderSummary
    <<record>> OrderSummary
    class OrderResponse
    <<record>> OrderResponse
    class OrderView
    <<record>> OrderView
    class OrderItemView
    <<record>> OrderItemView
    class PaymentView
    <<record>> PaymentView
    class PaymentUpdateRequest
    <<record>> PaymentUpdateRequest
    class QrCodeResponse
    <<record>> QrCodeResponse
  }
  namespace com_vms_order_entity {
    class Machine
    class MachineInventory
    class Order
    class OrderItem
    class Product
    class QrCode
    class Role
    <<enumeration>> Role
    class User
  }
  namespace com_vms_order_repository {
    class InventoryRepository
    <<interface>> InventoryRepository
    class MachineRepository
    <<interface>> MachineRepository
    class OrderItemRepository
    <<interface>> OrderItemRepository
    class OrderRepository
    <<interface>> OrderRepository
    class QrCodeRepository
    <<interface>> QrCodeRepository
  }
  namespace com_vms_order_security {
    class SecurityConfig
  }
  namespace com_vms_order_security_internal {
    class CachedBodyHttpServletRequest
    class HmacVerifier
    class InternalServiceAuthFilter
  }
  namespace com_vms_order_security_jwt {
    class JwtAuthenticationFilter
    class JwtService
  }
  namespace com_vms_order_service {
    class ApiException
    class InvalidOrderStateTransitionException
    class OrderService
    class DispenseResult
    <<record>> DispenseResult
    class QrPayloadService
    class DecodedPayload
    <<record>> DecodedPayload
  }

  InventoryRepository --|> JpaRepository
  MachineRepository --|> JpaRepository
  OrderItemRepository --|> JpaRepository
  OrderRepository --|> JpaRepository
  QrCodeRepository --|> JpaRepository
  CachedBodyHttpServletRequest --|> HttpServletRequestWrapper
  InternalServiceAuthFilter --|> OncePerRequestFilter
  JwtAuthenticationFilter --|> OncePerRequestFilter
  ApiException --|> RuntimeException
  InvalidOrderStateTransitionException --|> RuntimeException
```

## payment-service

```mermaid
classDiagram
  namespace com_vms_payment {
    class PaymentServiceApplication
  }
  namespace com_vms_payment_api {
    class ApiError
    <<record>> ApiError
    class ApiExceptionHandler
    class PaymentController
    class StripeWebhookController
  }
  namespace com_vms_payment_client {
    class OrderServiceClient
  }
  namespace com_vms_payment_config {
    class StripeConfig
    class WebClientConfig
  }
  namespace com_vms_payment_dto {
    class CreatePaymentIntentRequest
    <<record>> CreatePaymentIntentRequest
    class CreatePaymentIntentResponse
    <<record>> CreatePaymentIntentResponse
    class OrderPaymentUpdateRequest
    <<record>> OrderPaymentUpdateRequest
  }
  namespace com_vms_payment_entity {
    class Order
    class Payment
    class PaymentStatus
    <<enumeration>> PaymentStatus
    class ProcessedWebhookEvent
  }
  namespace com_vms_payment_payment_service {
    class PaymentServiceApplicationTests
  }
  namespace com_vms_payment_repository {
    class OrderRepository
    <<interface>> OrderRepository
    class PaymentRepository
    <<interface>> PaymentRepository
    class ProcessedWebhookEventRepository
    <<interface>> ProcessedWebhookEventRepository
  }
  namespace com_vms_payment_security {
    class HmacSignerService
    class SecurityConfig
  }
  namespace com_vms_payment_security_jwt {
    class JwtAuthenticationFilter
    class JwtService
  }
  namespace com_vms_payment_service {
    class ApiException
    class OrderService
    class PaymentService
    class StripeService
  }

  OrderRepository --|> JpaRepository
  PaymentRepository --|> JpaRepository
  ProcessedWebhookEventRepository --|> JpaRepository
  JwtAuthenticationFilter --|> OncePerRequestFilter
  ApiException --|> RuntimeException
```

