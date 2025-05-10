### TestCraft AI





## AI-Powered Educational Platform

TestCraft AI is an innovative educational platform that leverages artificial intelligence to facilitate the work of teachers and improve the educational process. The application enables automated generation of knowledge testing materials based on existing educational content, saving educators valuable time while ensuring comprehensive assessment coverage.

## 🌟 Features

### Core Functionality

- **AI-Powered Test Generation**: Automatically create multiple-choice and true/false questions from uploaded educational materials
- **Material Management**: Upload, organize, and manage teaching materials in various formats (PDF, DOCX, TXT)
- **Subject Organization**: Categorize materials and tests by subject for better organization
- **User Role Management**: Different interfaces and capabilities for teachers, students, and administrators


### For Teachers

- **Material Upload**: Import teaching materials in multiple formats (PDF, DOCX, TXT)
- **AI Test Generation**: Generate tests with various question types based on uploaded content
- **Test Export**: Download tests in PDF format or create Google Forms
- **Flashcard Creation**: Generate learning flashcards for key terms and concepts


### For Students

- **Test Access**: Take tests assigned by teachers
- **Flashcard Study**: Use AI-generated flashcards for effective learning
- **Progress Tracking**: View test results and track learning progress


### For Administrators

- **User Management**: Manage user accounts and roles
- **System Monitoring**: Monitor system performance and usage
- **Content Oversight**: Review and manage educational content


## 🛠️ Technology Stack

- **Backend**: Java 21 with Spring Boot 3.2.3
- **Frontend**: Thymeleaf, Bootstrap 5, HTML/CSS/JavaScript
- **Database**: PostgreSQL 15
- **AI Integration**: OpenAI API for content analysis and question generation
- **External API**: Google Classroom API for test distribution
- **Document Processing**: Apache PDFBox and Apache POI for file handling
- **Security**: Spring Security for authentication and authorization
- **Containerization**: Docker and Docker Compose


## 📋 Prerequisites

- Docker and Docker Compose
- OpenAI API Key
- Google API Key (for Google Classroom integration)
- Java 21 (for local development)
- Maven (for local development)


## 🚀 Getting Started

### Using Docker (Recommended)

1. **Clone the repository**

```shellscript
git clone https://github.com/yourusername/testcraftai.git
cd testcraftai
```


2. **Set up environment variables**
   Create a `.env` file in the project root with the following variables:

```plaintext
OPENAI_API_KEY=your_openai_api_key
GOOGLE_API_KEY=your_google_api_key
```


3. **Build and start the application**

```shellscript
docker-compose up -d
```


4. **Access the application**
   Open your browser and navigate to `http://localhost:8080`


### Local Development Setup

1. **Clone the repository**

```shellscript
git clone https://github.com/yourusername/testcraftai.git
cd testcraftai
```


2. **Configure the database**
   Update `src/main/resources/application.properties` with your database credentials
3. **Set up environment variables**

```shellscript
export OPENAI_API_KEY=your_openai_api_key
export GOOGLE_API_KEY=your_google_api_key
```


4. **Build and run the application**

```shellscript
./mvnw spring-boot:run
```


5. **Access the application**
   Open your browser and navigate to `http://localhost:8080`


## 📂 Project Structure

```plaintext
testcraftai/
├── src/
│   ├── main/
│   │   ├── java/mk/ukim/finki/testcraftai/
│   │   │   ├── config/         # Configuration classes
│   │   │   ├── controller/     # MVC controllers
│   │   │   ├── model/          # Entity classes
│   │   │   ├── repository/     # Data access layer
│   │   │   ├── service/        # Business logic layer
│   │   │   └── TestcraftaiApplication.java
│   │   └── resources/
│   │       ├── static/         # Static resources (CSS, JS)
│   │       ├── templates/      # Thymeleaf templates
│   │       └── application.properties
│   └── test/                   # Test classes
├── .mvn/                       # Maven wrapper
├── docker-compose.yml          # Docker Compose configuration
├── Dockerfile                  # Docker build instructions
├── mvnw                        # Maven wrapper script
├── pom.xml                     # Maven dependencies
└── README.md                   # Project documentation
```

## 🔐 Authentication and Authorization

The application implements a comprehensive security system:

- **Registration**: Users can register with username, email, password, and role selection
- **Login**: Secure authentication with Spring Security
- **Role-Based Access**: Different dashboards and capabilities for teachers, students, and administrators
- **Password Encryption**: BCrypt encryption for secure password storage

## 📝 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👥 Contributors

- Jane Panov