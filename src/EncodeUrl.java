import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;

import java.net.URLEncoder;
import java.util.List;

/**
 * Created by ishizono on 15/10/06.
 */
public class EncodeUrl extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getProject();
        if (project == null) {
            return;
        }

        Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
        if (editor == null) {
            return;
        }

        // カレントキャレットの位置を取得する
        List<Caret> allCarets = editor.getCaretModel().getAllCarets();
        final Document document = editor.getDocument();
        String documentText = document.getText();

        final String contents;
        // ドキュメントを読み込み、文字列を StringBuffer で組み立てる
        try {
            int startIdx = 0;
            StringBuilder sb = new StringBuilder();
            for(Caret caret : allCarets){
                int start = caret.getSelectionStart();
                int end = caret.getSelectionEnd();
                sb.append(documentText.substring(startIdx, start));
                String encodeStr = URLEncoder.encode(caret.getSelectedText(), "UTF-8");
                encodeStr = encodeStr.replace("*", "%2a");
                encodeStr = encodeStr.replace("-", "%2d");
                encodeStr = encodeStr.replace("+", "%20");
                encodeStr = encodeStr.replace("%0A", "%0D%0A");
                sb.append(encodeStr);
                startIdx = end;

            }
            sb.append(documentText.substring(startIdx, documentText.length()));
            contents = sb.toString();
        } catch (Exception e1) {
            return;
        }
        final Runnable readRunner = new Runnable() {
            @Override
            public void run() {
                document.setText(contents);
            }
        };
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                CommandProcessor.getInstance().executeCommand(project, new Runnable() {
                    @Override
                    public void run() {
                        ApplicationManager.getApplication().runWriteAction(readRunner);
                    }
                }, "DiskRead", null);
            }
        });
    }
}
