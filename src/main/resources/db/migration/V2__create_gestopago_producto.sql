CREATE TABLE IF NOT EXISTS tb_gestopago_producto (
    id_producto VARCHAR(100) PRIMARY KEY,
    nombre      VARCHAR(255),
    categoria   VARCHAR(100),
    precio      NUMERIC(12, 2),
    url_imagen  VARCHAR(500)
);
