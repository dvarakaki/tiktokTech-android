# Desafio Self — TikTokTech / turma G

Implementação baseada no `myself.zip` fornecido, integrada ao botão de câmera do projeto TikTokTech.

## Como usar

1. Sincronize o Gradle e execute o app no Android Studio.
2. Na tela inicial, toque no botão de câmera.
3. Use **Tirar foto** ou **Abrir Galeria** e confira a prévia.
4. Toque em **Enviar para o Cloudinary**.
5. A URL HTTPS aparece no texto de status. Toque no link para abrir no navegador ou pressione o texto para copiar. A foto precisa abrir para concluir a validação do enunciado.

Configuração: cloud name `dikitk54o`, preset unsigned `Tiktoktech`, folder `tiktoktech_salaG`. Não é necessário colocar API secret no aplicativo.

## Relação com a base

- `SelfActivity.java` adapta a MainActivity do myself, conservando os componentes `imgFoto`, `btnTirarFoto`, `btnGaleria`, `btnEnviar`, `progress`, `txtStatus` e os métodos `tirarFoto`, `abrirGaleria`, `salvarNuvem`.
- `activity_self.xml` adapta o LinearLayout vertical original com imagem, três botões e status.
- O SDK Cloudinary é `3.1.2`, igual ao ZIP.
- A inicialização e o UploadCallback ficam em `TikTokApp.java` para que uma rotação não perca o envio nem atualize a Activity antiga.
- A galeria usa ACTION_PICK com MediaStore.Images.Media.EXTERNAL_CONTENT_URI, como na base. O resultado passa por selecionarFoto para atualizar a prévia e o arquivo usado no envio.
- Foram acrescentados tratamento de cancelamento/erro, bloqueio de envio repetido e redução da prévia para fotos grandes.
- Não há cópia automática adicional na galeria: o desafio exige o envio à nuvem. A captura fica no armazenamento do app até o upload.
- O feed e os arquivos de comentários originais continuam no projeto; persistência no Firestore pertence à etapa seguinte do enunciado.

Os arquivos originais alterados na primeira implementação foram guardados em `.self-backup`. A licença da base está em `LICENSE-myself.txt`.
