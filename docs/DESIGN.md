# Redesign LoveAll — conexões com personalidade

Referências e implementação revisadas em 12/11/2026. O redesign abrange o aplicativo Kotlin/Compose; contratos da API, dados, regras de cadastro, preços e permissões comerciais foram preservados.

## Referências analisadas

| Referência oficial | Padrão observado | Aplicação no LoveAll |
| --- | --- | --- |
| [Bumble Discover](https://bumble.com/en/features/discover/) | Fotos e perfis como centro da descoberta, com hierarquia clara para conhecer pessoas. | Capa imersiva, nome/cidade sobre a imagem, galeria horizontal e ações de curtir/passar sempre acessíveis. |
| [Hinge — How We Connect Daters](https://hinge.co/how-we-connect-daters) e [Discover Feed](https://help.hinge.co/hc/en-us/articles/36311592824595-Discover-Feed) | Mais espaço para personalidade e informações do perfil, além da foto. | Bio com destaque editorial, leitura mais confortável e acesso ao perfil completo. Não foram criados prompts ou campos que a API não oferece. |
| [Feeld — interesses e desejos](https://feeld.co/ask-feeld/how-to/what-are-interests-and-desires-on-feeld) | Expressão de identidade e formas de se conectar. | Identidades preservadas e exibidas em chips, tom acolhedor e preferências com seleção clara. Não foram inventados interesses compartilhados ou compatibilidade. |

As referências foram consultadas em páginas oficiais com conteúdo e exemplos de produto. Não houve teste dos apps concorrentes instalados nem coleta sistemática de avaliações de usuários. Os problemas abaixo foram identificados no próprio LoveAll, não atribuídos a concorrentes.

## Problemas da experiência anterior

A interface dependia de componentes Material padrão, com poucos elementos de marca. As telas repetiam blocos de texto e cards sem hierarquia; a descoberta reservava grande área vazia quando não havia foto; os formulários exibiam longas listas de opções; e o chat dedicava muito espaço aos comandos de moderação. A combinação fazia o produto parecer uma demonstração funcional.

## Linguagem visual

- **Paleta:** creme quente, rosa berry, ameixa, lavanda e verde suave. Texto usa papéis semânticos do Material; cores decorativas ficam em `BrandColors`.
- **Tipografia:** DM Serif Display nos títulos e Manrope no texto e controles. Pesos estáticos 400/600/700 da Manrope garantem leitura consistente desde o minSdk 26, sem depender de atributos de fonte variável das versões mais recentes do Android. As fontes são empacotadas e funcionam offline.
- **Ilustração:** composição vetorial original desenhada em Canvas/Compose, com personagens abstratos, cartões inclinados, órbitas e formas orgânicas. Serve à marca e não representa usuários ou perfis reais.
- **Componentes:** botões arredondados com gradiente e feedback de pressão, chips selecionáveis, superfícies editoriais, avatares com aro, navegação inferior flutuante e menus com hierarquia consistente.
- **Tema escuro:** superfícies e acentos específicos; o tema acompanha o sistema. Não é uma simples inversão de cores.

As licenças SIL Open Font License estão em `docs/licenses/` e também no APK, em `assets/licenses/`. Fontes obtidas do repositório oficial [Google Fonts](https://github.com/google/fonts): DM Serif Display e Manrope. Nenhum asset de concorrente foi incorporado.

## Mudanças por fluxo

1. **Boas-vindas:** marca, ilustração, promessa editorial curta e ações claras de entrar/cadastrar.
2. **Login e cadastro:** campos agrupados, progresso de cadastro e chips de identidade/preferências; senha permanece fora de estado salvo em disco.
3. **Descoberta e curtidas:** cards imersivos, múltiplas fotos com paginação, fallback de monograma estilizado e dados reais. A bio longa abre no perfil completo. Sem porcentagem fictícia de compatibilidade, status online inventado ou perfis de demonstração no produto.
4. **Matches:** avatares, prévia da conversa e contagem real de mensagens não lidas.
5. **Chat:** cabeçalho compacto, balões com largura limitada, compositor com envio por ícone e ações de chamadas/denúncia/bloqueio/desfazer match no menu identificado para acessibilidade.
6. **Perfil e edição:** cartão de identidade, atalhos claros, formulário por seções, edição de fotos e botão de salvar fixo fora da rolagem.
7. **Segurança:** apresentação acolhedora, dicas da API, lista de bloqueados e confirmações preservadas.
8. **Planos e pagamento:** apresentação editorial, saldo separado e cartões de plano com preços e benefícios carregados do servidor. Aviso de indisponibilidade das chamadas e restrições existentes de checkout continuam visíveis.

## Acessibilidade e comportamento

Alvos principais têm altura mínima de 48–56 dp. Controles por ícone possuem rótulos de acessibilidade; gênero continua selecionável com semântica Material; galerias e idades têm descrições. Formulários rolam, o compositor respeita o teclado, o conteúdo limita largura em telas maiores e títulos não possuem limite artificial de linhas.

Os pares principais de texto foram calculados em sRGB: primário claro/branco 6,11:1, corpo claro/fundo 5,43:1, corpo escuro/fundo 10,78:1 e texto branco/gradiente de marca entre 6,05:1 e 9,09:1 nos extremos. Esses cálculos não constituem auditoria WCAG completa, especialmente para fotografias dinâmicas. O gradiente escuro sob nome/cidade melhora a leitura das capas.

Animações de transição e pressão usam APIs Compose que acompanham a escala de duração do sistema; não há animação infinita de fundo. Haptics ocorrem somente em ações explícitas de curtir/passar. Referências técnicas: [Material 3](https://developer.android.com/develop/ui/compose/designsystems/material3), [acessibilidade de componentes](https://developer.android.com/develop/ui/compose/accessibility/api-defaults) e [fontes no Compose](https://developer.android.com/develop/ui/compose/text/fonts).

## Validação

Capturas e resultados do redesign ficam em `qa/redesign/`. Os testes instrumentados usam API simulada e perfis de teste, sem pagamentos ou dados reais. A validação visual cobre estados carregados, fallback sem foto, formulários, navegação e temas; não substitui testes com TalkBack em aparelhos físicos ou homologação comercial.
