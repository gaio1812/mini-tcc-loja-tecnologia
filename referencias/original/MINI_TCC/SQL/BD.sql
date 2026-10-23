
CREATE DATABASE BDLOJATECNOLOGIA;

USE BDLOJATECNOLOGIA;

CREATE TABLE TBCLIENTE (
    idcliente INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
    nomecliente VARCHAR(100) NOT NULL,
    cpfcliente VARCHAR(14) NOT NULL UNIQUE,
    telcliente VARCHAR(15),
    emailcliente VARCHAR(100),
    endcliente VARCHAR(150)
);

CREATE TABLE TBPRODUTO (
    idprod INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
    nomeprod VARCHAR(50) NOT NULL,
    categprod VARCHAR(40),
    marcprod VARCHAR(40),
    qtdprod INTEGER NOT NULL,
    precoprod DECIMAL(10,2) NOT NULL
);

CREATE TABLE TBVENDA (
    idvenda INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
    codcliente INTEGER NOT NULL,
    datavenda DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    formapagamento VARCHAR(30),
    statusvenda VARCHAR(20) NOT NULL DEFAULT 'PENDENTE',
    valorvenda DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    FOREIGN KEY (codcliente) REFERENCES TBCLIENTE (idcliente)
);

CREATE TABLE TBITEMVENDA (
    iditem INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
    codvenda INTEGER NOT NULL,
    codprod INTEGER NOT NULL,
    qtditem INTEGER NOT NULL,
    valorunitario DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (codvenda) REFERENCES TBVENDA (idvenda),
    FOREIGN KEY (codprod) REFERENCES TBPRODUTO (idprod)
) ENGINE=INNODB;


/* DADOS TBCLIENTE */
INSERT INTO TBCLIENTE
(nomecliente, cpfcliente, telcliente, emailcliente, endcliente)
VALUES
('João Silva', '111.111.111-11', '(17)99911-1111',
 'joao@email.com', 'Rua A, 100'),
('Maria Oliveira', '222.222.222-22', '(17)99922-2222',
 'maria@email.com', 'Rua B, 200'),
('Pedro Santos', '333.333.333-33', '(17)99933-3333',
 'pedro@email.com', 'Rua C, 300'),
('Ana Costa', '444.444.444-44', '(17)99944-4444',
 'ana@email.com', 'Rua D, 400');


/* DADOS TBPRODUTO */
INSERT INTO TBPRODUTO
(nomeprod, categprod, marcprod, qtdprod, precoprod)
VALUES
('Notebook', 'Computadores', 'Dell', 15, 3500.00),
('Mouse Gamer', 'Perifericos', 'Logitech', 40, 150.00),
('Teclado Mecanico', 'Perifericos', 'Redragon', 25, 280.00),
('Monitor 24', 'Monitores', 'Samsung', 20, 899.90),
('Headset Gamer', 'Audio', 'HyperX', 30, 220.00),
('SSD 1TB', 'Armazenamento', 'Kingston', 18, 450.00),
('Memoria RAM 16GB', 'Componentes', 'Kingston', 22, 320.00),
('Webcam HD', 'Acessorios', 'Logitech', 12, 180.00);


/* DADOS TBVENDA */
INSERT INTO TBVENDA
(codcliente, datavenda, formapagamento, statusvenda, valorvenda)
VALUES
(1, '2026-10-01 10:30:00', 'PIX', 'CONCLUIDA', 3800.00),
(2, '2026-10-02 14:15:00', 'CARTAO', 'CONCLUIDA', 899.90),
(3, '2026-10-03 09:20:00', 'DINHEIRO', 'CONCLUIDA', 430.00),
(1, '2026-10-04 16:40:00', 'PIX', 'CONCLUIDA', 450.00),
(4, '2026-10-05 11:00:00', 'CARTAO', 'PENDENTE', 280.00);


/* DADOS TBITEMVENDA */
INSERT INTO TBITEMVENDA
(codvenda, codprod, qtditem, valorunitario)
VALUES
(1, 1, 1, 3500.00),
(1, 2, 2, 150.00),
(2, 4, 1, 899.90),
(3, 2, 1, 150.00),
(3, 5, 1, 220.00),
(3, 2, 1, 150.00),
(4, 6, 1, 450.00),
(5, 3, 1, 280.00);

CREATE TABLE TBUSUARIO (
    idusuario INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nomeusuario VARCHAR(100) NOT NULL,
    emailusuario VARCHAR(100) NOT NULL UNIQUE,
    senhausuario VARCHAR(255) NOT NULL,
    tipousuario ENUM('ADMINISTRADOR', 'FUNCIONARIO')
        NOT NULL DEFAULT 'FUNCIONARIO'
);