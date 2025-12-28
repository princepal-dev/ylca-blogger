# Blogger API

A comprehensive REST API for a blogging platform built with Spring Boot, featuring user authentication, blog management, image uploads, and role-based access control.

**Created by Prince Pal**

## 🚀 Features

### Core Functionality
- **User Authentication & Authorization**
  - JWT-based authentication with HTTP-only cookies
  - Role-based access control (Admin, Collaborator)
  - Secure password hashing with BCrypt

- **User Management**
  - Admin user registration with secret key
  - Admin can create users with auto-generated credentials
  - Profile management for all users
  - User deletion (admin only)

- **Blog Management**
  - Full CRUD operations for blog posts
  - Rich text content support
  - Author-based filtering
  - Timestamp tracking (created/updated)

- **Image Support**
  - Multiple images per blog post
  - File upload with validation (JPEG, PNG, GIF, WebP)
  - Image ordering and management
  - Automatic cleanup on blog deletion
  - Static file serving

### Security Features
- JWT token authentication
- Role-based permissions
- Input validation and sanitization
- Secure file upload handling
- CSRF protection disabled for API
- CORS configuration ready

## 🛠️ Technology Stack

- **Backend Framework**: Spring Boot 4.0.1
- **Language**: Java 17
- **Database**: MySQL 8.0+
- **ORM**: Spring Data JPA with Hibernate
- **Security**: Spring Security with JWT
- **File Upload**: Spring Multipart
- **Build Tool**: Maven
- **Documentation**: Spring Boot Actuator (ready for Swagger)

## 📋 Prerequisites

- **Java**: JDK 17 or higher
- **MySQL**: 8.0 or higher
- **Maven**: 3.6+ (or use included Maven wrapper)
- **Git**: For version control

## 🔧 Installation & Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd blogger
```

### 2. Database Setup
Create a MySQL database:
```sql
CREATE DATABASE blogger_db;
```

Update database credentials in `src/main/resources/application.properties`:
```properties
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
```

### 3. File System Setup
Create the image upload directory:
```bash
mkdir -p uploads/images
```

### 4. Build and Run
Using Maven wrapper (recommended):
```bash
./mvnw clean install
./mvnw spring-boot:run
```

Or using system Maven:
```bash
mvn clean install
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## ⚙️ Configuration

### Application Properties
Key configuration options in `application.properties`:

```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/blogger_db?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=password

# JWT
spring.app.jwtSecret=your-256-bit-secret
spring.app.jwtExpirationMs=30000000
spring.app.authKey=your-admin-secret-key

# File Upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=50MB
app.upload.dir=uploads/images/

# Frontend (for CORS)
frontend.url=http://localhost:5173
```

## 📚 API Documentation

### Authentication Endpoints (`/api/auth`)

#### Register Admin
```http
POST /api/auth/signup
Content-Type: application/json

{
  "username": "admin",
  "password": "securepassword",
  "secretKey": "__SecretKey__"
}
```

#### Login
```http
POST /api/auth/signin
Content-Type: application/json

{
  "username": "admin",
  "password": "securepassword"
}
```
**Response**: Sets JWT cookie + user info

#### Create User (Admin Only)
```http
POST /api/auth/users
Authorization: Bearer {jwt-token}
Content-Type: application/json

{
  "fullName": "John Doe",
  "phoneNumber": "+1234567890"
}
```
**Response**: Generated credentials for the new user

#### Update User Profile (Admin)
```http
PUT /api/auth/users/{userId}
Authorization: Bearer {jwt-token}
Content-Type: application/json

{
  "fullName": "Updated Name",
  "phoneNumber": "+0987654321"
}
```

#### Update Own Profile
```http
PUT /api/auth/profile
Authorization: Bearer {jwt-token}
Content-Type: application/json

{
  "fullName": "My Updated Name",
  "phoneNumber": "+0987654321"
}
```

#### Delete User (Admin Only)
```http
DELETE /api/auth/users/{userId}
Authorization: Bearer {jwt-token}
```

#### Logout
```http
POST /api/auth/signout
```
**Response**: Clears JWT cookie

### Blog Endpoints (`/api/blogs`)

#### Create Blog
```http
POST /api/blogs
Authorization: Bearer {jwt-token}
Content-Type: application/json

{
  "title": "My First Blog Post",
  "description": "This is a comprehensive blog post content..."
}
```

#### Get All Blogs
```http
GET /api/blogs
```

#### Get Blog by ID
```http
GET /api/blogs/{id}
```

#### Get Blogs by Author
```http
GET /api/blogs/author/{authorId}
```

#### Get My Blogs
```http
GET /api/blogs/my
Authorization: Bearer {jwt-token}
```

#### Update Blog
```http
PUT /api/blogs/{id}
Authorization: Bearer {jwt-token}
Content-Type: application/json

{
  "title": "Updated Blog Title",
  "description": "Updated content..."
}
```

#### Delete Blog
```http
DELETE /api/blogs/{id}
Authorization: Bearer {jwt-token}
```

### Image Endpoints (`/api/blogs`)

#### Upload Single Image
```http
POST /api/blogs/{blogId}/images
Authorization: Bearer {jwt-token}
Content-Type: multipart/form-data

file: [image file]
displayOrder: 1
```

#### Upload Multiple Images
```http
POST /api/blogs/{blogId}/images/multiple
Authorization: Bearer {jwt-token}
Content-Type: multipart/form-data

files: [image1.jpg, image2.png]
displayOrders: [1, 2]
```

#### Get Blog Images
```http
GET /api/blogs/{blogId}/images
```

#### Get Image by ID
```http
GET /api/blogs/images/{imageId}
```

#### Update Image Order
```http
PUT /api/blogs/images/{imageId}/order?displayOrder=2
Authorization: Bearer {jwt-token}
```

#### Delete Image
```http
DELETE /api/blogs/images/{imageId}
Authorization: Bearer {jwt-token}
```

## 🗄️ Database Schema

### Tables Created Automatically
- `users` - User accounts and profiles
- `blogs` - Blog posts with author relationships
- `images` - Image metadata with blog relationships

### Key Relationships
- User (1) → Blog (Many)
- Blog (1) → Image (Many)

## 🔒 Security & Permissions

### User Roles
- **ROLE_ADMIN**: Full access to all features
- **ROLE_COLLABORATOR**: Blog and image management

### Permission Matrix
| Feature | Admin | Collaborator |
|---------|-------|--------------|
| Create Users | ✅ | ❌ |
| Delete Users | ✅ | ❌ |
| Manage All Blogs | ✅ | ❌ |
| Manage Own Blogs | ✅ | ✅ |
| Upload Images | ✅ | ✅ |
| View All Content | ✅ | ✅ |

## 📁 Project Structure

```
blogger/
├── src/main/java/com/princeworks/blogger/
│   ├── BloggerApplication.java          # Main application class
│   ├── config/                          # Configuration classes
│   │   ├── AppConfig.java              # ModelMapper bean
│   │   └── WebMvcConfig.java           # Static resource config
│   ├── controller/                      # REST controllers
│   │   ├── AuthController.java         # Authentication & user management
│   │   └── BlogController.java         # Blog & image operations
│   ├── exceptions/                      # Custom exceptions
│   ├── model/                          # JPA entities
│   │   ├── User.java                   # User entity
│   │   ├── Blog.java                   # Blog entity
│   │   ├── Image.java                  # Image entity
│   │   └── AppRole.java                # Role enum
│   ├── payload/                        # DTOs
│   ├── repositories/                   # Data access layer
│   ├── security/                       # Security configuration
│   │   ├── jwt/                        # JWT utilities
│   │   ├── request/                    # Request DTOs
│   │   ├── response/                   # Response DTOs
│   │   ├── services/                   # User details services
│   │   └── WebSecurityConfig.java      # Security config
│   ├── service/                        # Business logic
│   └── util/                           # Utility classes
├── src/main/resources/
│   ├── application.properties          # Configuration
│   └── static/                         # Static resources
├── src/test/                           # Test classes
├── uploads/images/                     # Image storage (create manually)
├── pom.xml                            # Maven configuration
└── README.md                          # This file
```

## 🧪 Testing

### Run Tests
```bash
./mvnw test
```

### API Testing
Use tools like Postman, Insomnia, or curl to test endpoints. Import the following collection structure:

1. **Authentication Flow**
   - Register admin → Login → Get JWT token
   - Create users → Test role-based access

2. **Blog Management**
   - Create blog → Upload images → Update content → Delete

## 🚀 Deployment

### Production Checklist
- [ ] Update database credentials
- [ ] Configure JWT secret key
- [ ] Set up file storage permissions
- [ ] Configure CORS for frontend domain
- [ ] Set up reverse proxy (nginx/apache)
- [ ] Configure SSL certificates
- [ ] Set up log rotation
- [ ] Configure backup strategy

### Docker Support (Future Enhancement)
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines
- Follow Java naming conventions
- Add unit tests for new features
- Update documentation
- Ensure all tests pass
- Use meaningful commit messages

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For support, contact Prince Pal or create an issue in the repository.

## 👨‍💻 Author

**Prince Pal** - Project Creator & Developer

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- JWT.io for JWT implementation guidance
- MySQL team for the database
- All contributors and the open-source community

---

**Happy Blogging! 🎉**
