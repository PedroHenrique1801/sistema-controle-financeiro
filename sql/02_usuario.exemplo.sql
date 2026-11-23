-- Substitua a senha antes de executar como administrador. Nao publique sua copia editada.
-- Se o usuario ja existir, configure sua senha com ALTER USER em uma sessao local.
CREATE USER 'financeiro_app'@'localhost' IDENTIFIED BY 'COLOQUE_SUA_SENHA_AQUI';
GRANT SELECT, INSERT, UPDATE, DELETE ON controle_financeiro.* TO 'financeiro_app'@'localhost';
