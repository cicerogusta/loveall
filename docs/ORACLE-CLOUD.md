> **Alternativa histórica, substituída em 12/11/2026.** O cadastro brasileiro apresentado ao usuário só ofereceu Corporate. A opção atual é [Render + Supabase](RENDER-SUPABASE.md), com cadastro pessoal. Nenhuma VM Oracle foi criada ou publicada.

# Implantar a API na Oracle Cloud Always Free

O aplicativo continua exclusivamente Android. Este procedimento hospeda somente a API Node.js, o SQLite e as fotos. Estado atual: arquivos preparados localmente; **nenhuma conta, VM ou API pública foi criada por esta preparação**.

## 1. Criar a conta

Abra o [cadastro oficial](https://signup.cloud.oracle.com/). O titular deve preencher seus dados, confirmar email, configurar autenticação e concluir a verificação solicitada pela Oracle. A Oracle informa que utiliza cartão para verificação, podendo haver autorização temporária; não confundir esse procedimento com um pagamento de hospedagem. Não envie senha, dados de cartão ou chaves privadas pelo chat.

Escolha a região principal perto dos usuários, conforme disponibilidade, e use somente recursos com identificação **Always Free eligible**. Os créditos de teste de 30 dias não significam que qualquer recurso é gratuito permanentemente. Não é necessário migrar a conta para Pay As You Go para executar este projeto no limite gratuito.

Referências: [FAQ do cadastro](https://www.oracle.com/cloud/free/faq/) e [recursos Always Free](https://docs.oracle.com/en-us/iaas/Content/FreeTier/freetier_topic-Always_Free_Resources.htm).

## 2. Criar a máquina Linux

No Console: Compute → Instances → Create instance.

- Nome sugerido: `loveall-api`.
- Imagem: **Ubuntu 24.04 LTS**, compatível com a arquitetura escolhida e identificada como gratuita.
- Preferência: `VM.Standard.A1.Flex` (ARM), com **1 OCPU e 6 GB de RAM**, se disponível como Always Free na sua conta. Esse tamanho está dentro dos limites consultados e deixa espaço para compilar SQLite/Sharp. Conferir os limites mostrados no Console antes de criar.
- Disco de inicialização: cerca de 50 GB, dentro da franquia Always Free e sem aumentar desempenho ou contratar extras.
- Rede: sub-rede pública e Internet Gateway, com IP público. Não são necessários load balancer, NAT Gateway ou banco gerenciado.
- SSH: salvar a chave privada em local protegido no seu computador; a VM recebe somente a chave pública.

Se a Oracle informar falta de capacidade, aguardar disponibilidade ou consultar outro domínio de disponibilidade da região principal. Não trocar automaticamente por uma máquina paga. A alternativa Micro AMD tem só 1 GB de memória e oferece menos margem para compilar e processar fotos.

Na lista de segurança ou no NSG da VM, permitir:

| Porta | Origem | Uso |
| --- | --- | --- |
| TCP 22 | Seu IP público com `/32` | Administração SSH |
| TCP 80 | Internet | Redirecionamento e validação de certificado |
| TCP 443 | Internet | API HTTPS |

**Não abrir a porta 3001.** O Compose da Oracle não publica essa porta. Verificar também as regras do firewall do Ubuntu sem limpar as regras existentes. O Docker pode tratar portas publicadas de forma diferente do UFW; a lista de segurança da Oracle continua sendo necessária.

Referências: [criar instância](https://docs.oracle.com/en-us/iaas/Content/Compute/Tasks/launchinginstance.htm) e [firewall com Docker](https://docs.docker.com/engine/install/ubuntu/#firewall-limitations).

## 3. Endereço gratuito e HTTPS

Sem domínio próprio, pode-se começar com um hostname provisório de `sslip.io`. Se o IP fosse `203.0.113.10`, o formato seria:

```text
loveall-api.203-0-113-10.sslip.io
```

O IP acima é ilustrativo e não deve ser usado. Substituir pelo IP público real. O DNS retorna o IP embutido no nome, sem registrar um domínio. Caddy solicita e renova o certificado público quando o DNS resolve corretamente e as portas 80/443 estão acessíveis. Isso é configurado na VM; não é um certificado autoassinado a instalar no Android.

É um endereço de terceiro para os primeiros testes, não um domínio que passa a pertencer ao projeto. Sua disponibilidade, políticas e limites de certificados dependem dos prestadores. Se o IP mudar, o hostname também muda e será necessário atualizar a URL do Android. Um domínio próprio pode substituí-lo depois.

Referências: [serviço nip.io/sslip.io](https://nip.io/) e [HTTPS automático do Caddy](https://caddyserver.com/docs/automatic-https).

## 4. Preparar o servidor

Conectar por SSH como `ubuntu`, usando a chave correspondente e conferindo a identidade do servidor. Instalar Docker Engine e o plugin Compose pelo [procedimento oficial para Ubuntu](https://docs.docker.com/engine/install/ubuntu/). Não é necessário instalar Node diretamente no sistema: a imagem da API usa Node 22.

Os comandos abaixo pressupõem que o usuário SSH tem permissão para usar Docker. Se seguir a opção de grupo `docker` da instalação oficial, desconectar e reconectar antes de continuar. Validar:

```bash
docker version
docker compose version
```

## 5. Enviar o pacote do Windows

Na raiz do projeto, criar um pacote com os arquivos salvos atualmente, sem depender de commit ou de uma versão antiga do GitHub:

```powershell
.\scripts\New-OracleDeploymentPackage.ps1
```

O resultado fica em `qa/oracle/loveall-api.tar.gz`. Inclui apenas o Dockerfile, o código/dependências declaradas da API e os arquivos de implantação. **Não inclui `.env`, banco, fotos, chaves ou código Android.**

Enviar, substituindo os exemplos por IP e caminho reais:

```powershell
scp -i "C:\caminho\chave-oracle.key" .\qa\oracle\loveall-api.tar.gz ubuntu@IP_PUBLICO:~/loveall-api.tar.gz
ssh -i "C:\caminho\chave-oracle.key" ubuntu@IP_PUBLICO
```

Na primeira conexão, confirmar a impressão digital do servidor pelos dados da instância. Não desabilitar a conferência de chave SSH.

## 6. Configurar e iniciar

Na VM:

```bash
mkdir -p ~/loveall
tar -xzf ~/loveall-api.tar.gz -C ~/loveall
cd ~/loveall
bash deploy/oracle/prepare.sh SEU_HOSTNAME_REAL
bash deploy/oracle/deploy.sh
```

`prepare.sh` cria `deploy/oracle/.env` com permissão restrita e segredo JWT aleatório, sem imprimi-lo. Se o arquivo já existe, não o substitui nem troca o segredo. A configuração fica só na VM e não deve ser enviada ao GitHub.

`deploy.sh` valida o Compose, compila a imagem e inicia os serviços. Em atualizações, cria backup dos dados existentes antes de substituir o contêiner. O primeiro início do Caddy ainda precisa concluir a emissão do certificado: a saúde do contêiner da API não prova que o HTTPS público já está funcionando.

Usar o Compose de `deploy/oracle`, **não o `docker-compose.yml` da raiz**, para essa implantação. SQLite e fotos ficam no volume `api_data`; certificados têm volumes separados. A API executa como usuário sem privilégios, com sistema de arquivos somente leitura fora do volume e do diretório temporário. Os logs têm rotação para limitar consumo do disco.

O Caddy substitui os cabeçalhos encaminhados pelo cliente e a API confia em somente um salto de proxy. A porta interna não pode ficar acessível por um caminho externo alternativo.

## 7. Verificar antes de usar dados reais

```bash
cd ~/loveall/deploy/oracle
docker compose --env-file .env -f compose.yaml ps
docker compose --env-file .env -f compose.yaml logs --tail=60 api caddy
curl --fail https://SEU_HOSTNAME_REAL/api/health
```

Não utilizar `curl -k`: o Android precisa de um certificado público válido. Conferir o mesmo endereço de outro dispositivo/rede. Validar cadastro de teste, login, fotos e persistência após reinício. Não importar automaticamente o banco de desenvolvimento: esta implantação começa com uma base nova.

Somente depois dessa validação, a URL do aplicativo será:

```text
https://SEU_HOSTNAME_REAL/api/
```

Atualizar `loveall.apiBaseUrl` com o endereço verificado; nenhum host fictício é colocado no release do app por esta preparação. A assinatura e publicação na Play continuam sendo etapas separadas.

## 8. Backup e manutenção

```bash
cd ~/loveall
bash deploy/oracle/backup.sh
```

O script pausa a API brevemente, copia SQLite/WAL/fotos do mesmo volume e retoma a API se ela estava em execução. O arquivo fica em `deploy/oracle/backups`, com permissão restrita. Uma atualização também chama esse procedimento quando há contêiner anterior. Não usar `docker compose down --volumes`, pois isso remove os dados persistidos.

Copiar backups protegidos para fora da VM e testar restauração em volume separado antes da abertura pública. Uma cópia no próprio servidor não protege contra perda da VM, e as instâncias gratuitas ociosas podem ser recolhidas pela Oracle. Backups e segredos contêm dados privados; não os incluir em pacote, commit ou logs. Definir retenção e não restaurar contas excluídas sem um procedimento que preserve as exclusões.

Não há agendamento de backup criado por esta preparação. Frequência, retenção, destino externo e teste de restauração ainda precisam ser definidos na conta real.

## Limites da validação local

Foram executados 11 testes da API, sem falhas, incluindo os testes de interpretação do IP atrás do proxy. A sintaxe Bash foi conferida; o preparador foi testado com hostname inválido, criação privada do segredo e recusa de sobrescrita. O pacote foi inspecionado para excluir segredos e dados de usuários. [Resultados](../qa/oracle/preparation-results.json).

Este ambiente não possui Docker Engine/WSL Linux ativo; a imagem ARM, o volume, o proxy e a emissão TLS precisam ser executados e homologados na VM. O CI passou a prever build e inicialização da imagem, persistência e validação do Caddy, mas esse job ainda não foi executado no GitHub. Não declarar a API publicada antes da homologação.
