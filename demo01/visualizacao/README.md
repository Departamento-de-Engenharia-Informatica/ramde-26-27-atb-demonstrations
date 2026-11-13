# Ver os diagramas da demo01 no VS Code

Abre `lightswitch.png` para ver os quatro requisitos do interruptor e a ligação
`REQ04 → REQ01`. Abre `horta_exemplo.png` para ver o exemplo didático da horta.
O VS Code mostra os PNG diretamente. Os SVG são versões vetoriais das mesmas
imagens, úteis para ampliar ou incluir num relatório.

Os `.puml` desta pasta são fotografias preparadas manualmente a partir dos
modelos `.reqdsl`. Renderizámo-los com PlantUML. Ainda não executámos o parser
Xtext nem o conversor Java neste percurso; alterar um `.reqdsl` não atualiza
automaticamente estas fotografias.

## O que cada ficheiro faz

| Ficheiro | Papel |
| --- | --- |
| [`req.ecore`](../req/model/req.ecore) | Define `Requirement`, os seus atributos e a referência `dependsOn`. |
| [`ReqDsl.xtext`](../reqdsl/org.xtext.example.reqdsl/src/org/xtext/example/reqdsl/ReqDsl.xtext) | Define como escrever requisitos e referências em texto. |
| [`lightswitch.reqdsl`](../runtime-projects/testReqDSL/model/lightswitch.reqdsl) | Contém os quatro requisitos concretos e a dependência que acrescentaste. |
| [`EMFToPlantUMLConverter.java`](../req/src/req/EMFToPlantUMLConverter.java) | Recebe o modelo EMF, percorre requisitos e dependências e produz texto PlantUML. |
| `lightswitch.puml` | Fotografia desse texto, preparada para este exercício. |
| `lightswitch.png` / `lightswitch.svg` | Imagens obtidas ao renderizar o `.puml`. |

No percurso do professor, o editor Xtext lê o `.reqdsl` e constrói o modelo EMF.
O provider de diagramas chama o conversor Java e a vista Eclipse `DSL Diagram`
renderiza o resultado. O método `generatePlantUMLText` não tem um `main` para
executar diretamente com o botao Run do VS Code.

## Como ler a seta

No modelo escreveste, dentro de `Requirement REQ04`:

```text
dependsOn (REQ01)
```

O Java percorre `req.getDependsOn()` e produz:

```plantuml
REQ04 .> REQ01 : dependsOn
```

A seta parte do requisito dependente (`REQ04`) e aponta para o requisito de
que depende (`REQ01`). O Java desenha a relação que declaraste. Não decide que
requisitos devem depender de outros.

Sem essa declaração, continuariam a existir quatro caixas, mas não haveria
aquela seta. A ausência de setas em `REQ02` e `REQ03` significa que o modelo
atual não declarou dependências para eles.

## Exemplo para a horta

Modelo: [`horta_exemplo.reqdsl`](../runtime-projects/testReqDSL/model/horta_exemplo.reqdsl).
É um exemplo didático para treinar a mesma linguagem. Estes requisitos precisam
de ser revistos antes de serem usados como requisitos do projeto.

| Requisito | Descrição | Depende de |
| --- | --- | --- |
| `H01` | Disponibilizar uma leitura da humidade do solo. | — |
| `H02` | Decidir a necessidade de rega com base nessa leitura. | `H01` |
| `H03` | Iniciar a rega quando a decisão indicar necessidade. | `H02` |

No gráfico, `H03 → H02 → H01`. Segue as setas: iniciar a rega
depende da decisão; a decisão depende da leitura.

Este é um diagrama de dependências entre requisitos. A seta não representa
uma sequência temporal. O texto dos requisitos também não implementa o controlo
da rega: descreve o comportamento pretendido.

Para este exemplo reutilizas o mesmo Ecore, a mesma gramática e o mesmo conversor.
Mudam os requisitos concretos do `.reqdsl`. Se quiseres acrescentar um atributo
novo, como `priority`, será necessário rever o metamodelo, a gramática e a
representação no diagrama.

## Treino com um resultado visível

1. Abre `horta_exemplo.puml` e `horta_exemplo.png` lado a lado.
2. Explica a linha `H03 .> H02 : dependsOn` e encontra `dependsOn (H02)` no modelo.
3. Para experimentar mais uma dependência, muda no modelo o bloco `H03` para
   `dependsOn (H02, H01)` e acrescenta `H03 .> H01 : dependsOn` no `.puml`.
4. Executa os comandos abaixo. Espera ver tres caixas e tres setas.
5. Para voltar ao exemplo inicial, retira apenas as duas alterações desse passo.

Neste treino atualizas manualmente a fotografia `.puml`. A geração automática
a partir do modelo pertence ao percurso Xtext/EMF/Java descrito acima.

## Gerar novamente as imagens

No terminal, a partir da raiz deste repositório:

```bash
env -u DISPLAY plantuml -charset UTF-8 -tpng demo01/visualizacao/lightswitch.puml demo01/visualizacao/horta_exemplo.puml
env -u DISPLAY plantuml -charset UTF-8 -tsvg demo01/visualizacao/lightswitch.puml demo01/visualizacao/horta_exemplo.puml
```

Os comandos leem os `.puml` e atualizam os PNG/SVG ao lado. O PlantUML e o
Graphviz já estão disponíveis nesta máquina. `env -u DISPLAY` permite renderizar
sem abrir uma janela gráfica.

Referência: [documentação oficial do PlantUML para diagramas de objetos](https://plantuml.com/object-diagram).
