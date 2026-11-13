USE BodexaT3;
GO
SET NOCOUNT ON;
SET QUOTED_IDENTIFIER ON;
SET ANSI_NULLS ON;
SET ANSI_PADDING ON;
SET ANSI_WARNINGS ON;
SET ARITHABORT ON;
SET CONCAT_NULL_YIELDS_NULL ON;
SET NUMERIC_ROUNDABORT OFF;
SET XACT_ABORT OFF;
BEGIN TRANSACTION;
BEGIN TRY
    INSERT INTO Categoria(id,nombre) VALUES(2147483000,N'Prueba temporal T3');
    INSERT INTO Producto VALUES(2147483000,N'Producto temporal',4.50,6,2147483000);
    INSERT INTO Venta VALUES(2147483000,SYSDATETIME());
    INSERT INTO DetalleVenta VALUES(2147483000,2147483000,N'Producto temporal',2,4.50);
    UPDATE Producto SET stock=stock-2 WHERE id=2147483000;
    INSERT INTO AlertaStock VALUES(2147483000,2147483000,SYSDATETIME(),0);
    IF (SELECT stock FROM Producto WHERE id=2147483000)<>4 THROW 51000,'Stock inesperado',1;
    PRINT 'SQL01 APROBADO: tablas, relaciones, venta y stock 6 -> 4 en transaccion.';
    DECLARE @rechazo BIT=0;
    BEGIN TRY UPDATE Producto SET stock=-1 WHERE id=2147483000; END TRY
    BEGIN CATCH SET @rechazo=1; END CATCH;
    IF @rechazo=0 THROW 51000,'Se permitio stock negativo',1;
    PRINT 'SQL02 APROBADO: restriccion impide stock negativo.';
    SET @rechazo=0;
    BEGIN TRY INSERT INTO AlertaStock VALUES(2147483001,2147483000,SYSDATETIME(),0); END TRY
    BEGIN CATCH SET @rechazo=1; END CATCH;
    IF @rechazo=0 THROW 51000,'Se permitio alerta pendiente duplicada',1;
    PRINT 'SQL03 APROBADO: indice impide dos alertas pendientes por producto.';
    ROLLBACK;
    IF EXISTS(SELECT 1 FROM Venta WHERE id=2147483000) THROW 51000,'Rollback incompleto',1;
    PRINT 'SQL04 APROBADO: rollback completo; no quedan datos de prueba.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT>0 ROLLBACK;
    THROW;
END CATCH;
GO
