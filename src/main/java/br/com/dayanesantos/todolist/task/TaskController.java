package br.com.dayanesantos.todolist.task;

import br.com.dayanesantos.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.config.Task;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private ITaskRepository iTaskRepository;

    @PostMapping("/")
    public ResponseEntity createTask(@RequestBody TaskModel taskModel, HttpServletRequest request) {
        var idUser = request.getAttribute("idUser");
        taskModel.setIdUser((UUID) idUser);

        var currentDate = LocalDateTime.now();
        if(currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt()) ){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("A data de ínicio / data de término deve ser maior que a data atual.");
        }
        if(taskModel.getStartAt().isAfter(taskModel.getEndAt())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("A data de ínicio deve ser menor que a data de término.");
        }
        var task = this.iTaskRepository.save(taskModel);
        return ResponseEntity.status(HttpStatus.OK).body(task);
    }

    @GetMapping("/")
    public List<TaskModel> list(HttpServletRequest request) {
        var idUser = request.getAttribute("idUser");

        var tasks = this.iTaskRepository.findByIdUser((UUID) idUser);
        return tasks;
    }


    @PutMapping("/{taskId}")
    public ResponseEntity updateTask(@RequestBody TaskModel taskModel, @PathVariable UUID taskId, HttpServletRequest request) {
        var task = this.iTaskRepository.findById(taskId).orElse(null);
        var idUser = request.getAttribute("idUser");

        //verifica se a tarefa existe
        if(task == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tarefa não encontrada.");
        }

        //verifica se o id do usuario dono da task é o mesmo id do usuario autenticado.
        if(!task.getIdUser().equals(idUser)) {
             return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Usuário não tem permissão para alterar essa tarefa.");
        }


        Utils.copyNonNullProperties(taskModel, task);
        var taskUpdated = this.iTaskRepository.save(task);
        return ResponseEntity.ok(taskUpdated);
    }
}
