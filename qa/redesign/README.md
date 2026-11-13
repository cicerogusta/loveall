# Redesign — validação final

Validado em 12/11/2026 no emulador Android 17/API 37, com JDK 17 e SDK de compilação 36. Referências, linguagem visual e decisões estão em [docs/DESIGN.md](../../docs/DESIGN.md).

## Resultados

- APK debug gerado: `app/build/outputs/apk/debug/app-debug.apk`.
- 51 testes JVM aprovados, sem falhas.
- 9 testes instrumentados aprovados no tema claro e 9 no tema escuro.
- 6 fluxos instrumentados aprovados com texto ampliado a 150% na composição.
- Total: 24 execuções instrumentadas aprovadas, incluindo persistência criptografada e fluxos nativos de cadastro, login, mensagens, segurança e planos.
- Android Lint: **0 erros, 22 avisos** remanescentes de versões, recomendações KTX e diretório de recursos. Nenhuma checagem foi suprimida para aprovar o redesign.
- Verificação de cobertura das regras locais aprovada; escopo e meta de 80% preservados.
- Bundle de release com R8 compilado com `https://build-check.invalid/api/`, exclusivamente para validar o build, sem serviço real, publicação ou pagamentos.

O teste do menu de conversa confirma que chamadas, denúncia, bloqueio e desfazer match continuam acessíveis. A denúncia é enviada ao backend simulado e a confirmação é verificada; o teste não concede permissões pagas nem inicia chamadas fictícias.

## Capturas

As versões finais estão em:

- `light/`: boas-vindas, descoberta, matches, conversa, perfil, cadastro, edição, segurança e planos.
- `dark/`: os mesmos fluxos no tema escuro.
- `large-text/`: os fluxos com escala de texto 1,5.

Os PNGs diretamente nesta pasta, fora dessas subpastas, pertencem à primeira rodada de inspeção visual. Para avaliar a entrega final, use as subpastas acima. Os perfis são fixtures de teste e o fallback sem foto é intencional; nenhuma pessoa ou informação fictícia foi adicionada ao aplicativo de produção.

Os screenshots são capturas reais da composição Android, sem retoque. Fotos reais carregadas pela API, tablets/dobráveis, TalkBack em aparelho físico e integrações de mídia/compras ainda exigem homologação apropriada; a validação não declara conformidade integral ou aprovação Google Play.

SHA-256 do APK debug validado: `699A5078925E7E5A84DFF56C23467EB866A131728D9AE04FC0437FF61ECB1056`.
