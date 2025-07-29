import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

public class PersonalTaskManager {

    private static final String TASKS_FILE = "tasks_database.json";
    private static final List<String> VALID_PRIORITIES = Arrays.asList("Low", "Medium", "High");
    private List<JSONObject> tasks;

    public PersonalTaskManager() {
        this.tasks = loadTasks();
    }

    private List<JSONObject> loadTasks() {
        List<JSONObject> loadedTasks = new ArrayList<>();
        if (!Files.exists(Paths.get(TASKS_FILE))) {
            return loadedTasks;
        }

        try (InputStream is = new FileInputStream(TASKS_FILE)) {
            JSONTokener tokener = new JSONTokener(is);
            JSONArray array = new JSONArray(tokener);
            for (int i = 0; i < array.length(); i++) {
                loadedTasks.add(array.getJSONObject(i));
            }
        } catch (Exception e) {
            System.out.println(" Không thể đọc file JSON. Tạo danh sách trống.");
        }
        return loadedTasks;
    }

    private void saveTasks(List<JSONObject> taskList) {
        JSONArray array = new JSONArray(taskList);
        try (FileWriter file = new FileWriter(TASKS_FILE)) {
            file.write(array.toString(4)); // Ghi JSON đẹp
        } catch (IOException e) {
            System.out.println(" Ghi file thất bại: " + e.getMessage());
        }
    }

    private boolean isTitleValid(String title) {
        return title != null && !title.trim().isEmpty();
    }

    private LocalDate parseDueDate(String dateString) {
        try {
            return LocalDate.parse(dateString);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private boolean isPriorityValid(String priority) {
        return VALID_PRIORITIES.contains(priority);
    }

    private boolean isDuplicate(List<JSONObject> taskList, String title, LocalDate dueDate) {
        for (JSONObject task : taskList) {
            if (task.getString("title").equalsIgnoreCase(title) &&
                task.getString("due_date").equals(dueDate.toString())) {
                return true;
            }
        }
        return false;
    }

    private JSONObject buildTask(String title, LocalDate dueDate, String priority) {
        JSONObject task = new JSONObject();
        task.put("title", title);
        task.put("due_date", dueDate.toString());
        task.put("priority", priority);
        return task;
    }

    public void addNewTask(String title, String dueDateStr, String priority) {
        if (!isTitleValid(title)) {
            System.out.println(" Tiêu đề không được để trống.");
            return;
        }

        LocalDate dueDate = parseDueDate(dueDateStr);
        if (dueDate == null) {
            System.out.println(" Ngày không hợp lệ. Định dạng đúng là yyyy-MM-dd.");
            return;
        }

        if (!isPriorityValid(priority)) {
            System.out.println(" Mức độ ưu tiên không hợp lệ. Chấp nhận: Low, Medium, High.");
            return;
        }

        List<JSONObject> taskList = loadTasks();

        if (isDuplicate(taskList, title, dueDate)) {
            System.out.println(" Nhiệm vụ đã tồn tại."); 
            return;
        }

        JSONObject task = buildTask(title, dueDate, priority);
        taskList.add(task);
        saveTasks(taskList);
        System.out.println(" Thêm nhiệm vụ thành công.");
    }
}

