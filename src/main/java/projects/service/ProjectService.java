package projects.service;

import projects.dao.ProjectDao;
import projects.entity.Project;


public class ProjectService {
  private ProjectDao projectDao = new ProjectDao();


  public Project addProject(Project project) {
    return projectDao.insertProject(project);
  }
}
//Created an instance of ProjectDao. calls the Dao to insert project row.
//return the value from method