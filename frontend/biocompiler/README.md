# DNA Sequence Analyzer

Vamos criar duas páginas: a primeira vai ser uma página com inserção de arquivo e/ou com digitação de um input de texto. O layout vai ser da seguinte forma: Teremos a navbar em cima a Logo/ nome "BioCompiler 1.0" disposto no lado esquerdo da navbar, o botão para mudar para o modo escuro (a tela vai ser no tema claro no padrão) e um botão para ver histórico. Esses dois estarão do lado direito. Os botões terão a cor secundária do sistema que é azul mas não escuro demais e nem claro demais ficando entre o indigo e o mediterrâneo. O input de texto que será para digitar uma sequência de DNA e o botão para inserir um arquivo ficarão alternando por um toggle slider que mudará de modo de arquivo para texto e vice-versa. Teremos um botão "analisar" que será para submissão do texto ou formulário.

Na segunda página que aparecerá quando apertar o botão de histórico, listará todos os dados em forma de tabela. Acima da tabela teremos cards com informações sobre quantos derão base inválida, quantos deram correto, quantos deram Start codon não encontrado, quantos deram stop codon não encontrados, quantos deram frame shift e quantos deram nonsense mutation. Dados deverão ser paginados na tabela. Você pode colocar mais coisas sugestivas acerca de design, mas não deve sair dessa lógica regrada que eu falei para as duas páginas

This project was built with [Lovable](https://lovable.dev).

## Build with Lovable

Continue developing this project in the [Lovable editor](https://lovable.dev/projects/1d969b62-d6ae-482e-b840-e8af7abcdf80).

- **Ship faster**: describe what you want to build and Lovable handles the code.
- **Stay in sync**: every change made in Lovable is committed straight to this repository.
- **Full ownership**: this code is yours. Push to `main` on GitHub and your changes sync back into Lovable, ready for your next prompt.

## Development

Prefer working locally? You need Node.js and npm — [install with nvm](https://github.com/nvm-sh/nvm#installing-and-updating).

```sh
git clone <this-repository-url>
cd <repository-name>
npm i
npm run dev
```
