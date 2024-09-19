# API de Administração de Frota

## Checklist de Implementação

### 1. Desenvolvimento da API
- [x] Desenvolver a API utilizando Java puro, sem frameworks.
- [x] Implementar o salvamento das tripulações no MongoDB.
- [x] Consumir a SWAPI para busca de dados de naves e tripulantes.

### 2. Operações de API
- [x] Buscar membros disponíveis da SWAPI e validar se já estão em alguma tripulação (com paginação).
- [x] Buscar naves disponíveis da SWAPI e validar se já estão sendo usadas (com paginação).
- [x] Buscar todas as tripulações cadastradas no banco de dados (com paginação).
- [x] Buscar uma tripulação específica pelo nome.
- [x] Criar tripulação, selecionando membros e nave, e salvá-la no MongoDB.
- [x] Remover tripulação do banco de dados pelo nome.
- [x] Editar tripulação, alterando membros.

### 3. Validações e Regras de Negócio
- [x] Validar se o membro ou nave já está em uso ao buscar.
- [x] Consultar se o tripulante ou nave realmente existem na SWAPI antes de inserir no banco de dados.

### 4. Cache e Performance
- [ ] Utilizar Redis para cachear as respostas das tripulações criadas no banco de dados.
- [ ] Limpar o cache ao criar, remover ou editar uma tripulação para manter as informações atualizadas.

### 5. Paginação
- [x] Implementar paginação nas rotas, incluindo a passagem de parâmetros de paginação para as chamadas à SWAPI.

### 6. Testes Unitários
- [ ] Criar testes unitários para todos os fluxos implementados.

### 7. Fluxo de Espionagem
- [ ] Implementar um mecanismo assíncrono para enviar os dados das tripulações atualizados para um arquivo JSON secreto sempre que uma frota for criada, editada ou removida.

### 8. Requisitos de Frota
- [x] Garantir que uma frota tenha entre 1 e 5 tripulantes.xs
- [x] Garantir que cada frota contenha uma nave.
