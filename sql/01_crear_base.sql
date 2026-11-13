
IF DB_ID(N'BodexaT3') IS NULL CREATE DATABASE BodexaT3;
GO
USE BodexaT3;
GO
SET QUOTED_IDENTIFIER ON;
SET ANSI_NULLS ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET ARITHABORT ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET NUMERIC_ROUNDABORT OFF;
IF OBJECT_ID('dbo.Categoria') IS NULL
BEGIN
    CREATE TABLE Categoria(id INT PRIMARY KEY CHECK(id>0), nombre NVARCHAR(50) NOT NULL UNIQUE CHECK(LEN(LTRIM(RTRIM(nombre)))>0));
    CREATE TABLE Producto(id INT PRIMARY KEY CHECK(id>0), nombre NVARCHAR(80) NOT NULL CHECK(LEN(LTRIM(RTRIM(nombre)))>0),
        precio DECIMAL(11,2) NOT NULL CHECK(precio>0), stock INT NOT NULL CHECK(stock>=0),
        categoria_id INT NOT NULL REFERENCES Categoria(id));
    CREATE TABLE Venta(id INT PRIMARY KEY CHECK(id>0), fecha DATETIME2 NOT NULL);
    CREATE TABLE DetalleVenta(venta_id INT NOT NULL REFERENCES Venta(id), producto_id INT NOT NULL REFERENCES Producto(id),
        nombre_producto NVARCHAR(80) NOT NULL, cantidad INT NOT NULL CHECK(cantidad>0),
        precio DECIMAL(11,2) NOT NULL CHECK(precio>0), PRIMARY KEY(venta_id,producto_id));
    CREATE TABLE AlertaStock(id INT PRIMARY KEY, producto_id INT NOT NULL REFERENCES Producto(id),
        fecha DATETIME2 NOT NULL, atendida BIT NOT NULL);
END;
IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE name='UX_AlertaPendiente' AND object_id=OBJECT_ID('dbo.AlertaStock'))
    CREATE UNIQUE INDEX UX_AlertaPendiente ON AlertaStock(producto_id) WHERE atendida=0;
GO

