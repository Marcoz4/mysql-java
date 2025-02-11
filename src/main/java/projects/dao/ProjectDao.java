package projects.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import projects.entity.Category;
import projects.entity.Material;
import projects.entity.Project;
import projects.entity.Step;
import projects.exception.DbException;
import provided.util.DaoBase;

public class ProjectDao extends DaoBase {
	private static final String CATEGORY_TABLE = "category";
	  private static final String MATERIAL_TABLE = "material";
	  private static final String PROJECT_TABLE = "project";
	  private static final String PROJECT_CATEGORY_TABLE = "project_category";
	  private static final String STEP_TABLE = "step";
//Added constants	  
	
	public Project insertProject(Project project) {
		  // @formatter:off
	    String sql = ""
	        + "INSERT INTO " + PROJECT_TABLE + " "
	        + "(project_name, estimated_hours, actual_hours, difficulty, notes) "
	        + "VALUES "
	        + "(?, ?, ?, ?, ?)";
	    // @formatter:on
//Wrote SQL statement it will insert values from insertProject()method

	    try(Connection conn = DbConnection.getConnection()) {
	        startTransaction(conn);

	        try(PreparedStatement stmt = conn.prepareStatement(sql)) {
	            setParameter(stmt, 1, project.getProjectName(), String.class);
	            setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
	            setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
	            setParameter(stmt, 4, project.getDifficulty(), Integer.class);
	            setParameter(stmt, 5, project.getNotes(), String.class);

	            stmt.executeUpdate();

	            Integer projectId = getLastInsertId(conn, PROJECT_TABLE);
	            commitTransaction(conn);

	            project.setProjectId(projectId);
	            return project;
	          }
	          catch(Exception e) {
	            rollbackTransaction(conn);
	            throw new DbException(e);
	          }
	        }
	        catch(SQLException e) {
	          throw new DbException(e);
	        }
	      }

	public List<Project> fetchAllProjects() {
		String sql = "SELECT * FROM " + PROJECT_TABLE + " ORDER BY project_name";
//C3 Wrote the SQL statement to return all projects. Order the results by project name
		
	  try(Connection conn = DbConnection.getConnection()) {
		      startTransaction(conn);	
// Obtaining the Connection object. Started a new transaction.

	    try(PreparedStatement stmt = conn.prepareStatement(sql)) {
		  try(ResultSet rs = stmt.executeQuery()) {
		    List<Project> projects = new LinkedList<>();
		    while(rs.next()) {
	            projects.add(extract(rs, Project.class));
	            /* Alternative approach that uses straight JDBC method calls. */
	            // Project project = new Project();
	            //
	            // project.setActualHours(rs.getBigDecimal("actual_hours"));
	            // project.setDifficulty(rs.getObject("difficulty", Integer.class));
	            // project.setEstimatedHours(rs.getBigDecimal("estimated_hours"));
	            // project.setNotes(rs.getString("notes"));
	            // project.setProjectId(rs.getObject("project_id", Integer.class));
	            // project.setProjectName(rs.getString("project_name"));
	            //
	            // projects.add(project);
	          }

	          return projects;
	        }
	      }
	      catch(Exception e) {
	        rollbackTransaction(conn);
	        throw new DbException(e);
	      }
	    }
	    catch(SQLException e) {
	      throw new DbException(e);
	    }
	}

	public Optional<Project> fetchProjectById(Integer projectId) {
		String sql = "SELECT * FROM " + PROJECT_TABLE + " WHERE project_id = ?";

	    try(Connection conn = DbConnection.getConnection()) {
	      startTransaction(conn);

	      /*
	       * This try block is used to wrap all code to return the project row and accompanying
	       * materials, steps and categories so that, if an error occurs at any place, the transaction
	       * can be rolled back correctly.
	       */
	      try {
	        Project project = null;
	        
	        try(PreparedStatement stmt = conn.prepareStatement(sql)) {
	            setParameter(stmt, 1, projectId, Integer.class);

	            /*
	             * Alternate approach. If you know your parameter will never be null you can set the
	             * parameter on the statement directly using JDBC.
	             */
	            // stmt.setInt(1, projectId);

	            try(ResultSet rs = stmt.executeQuery()) {
	              if(rs.next()) {
	                project = extract(rs, Project.class);
	              }
	            }
	          }

	          /*
	           * This null check isn't expressly needed because if the project ID is invalid, each method
	           * will simply return an empty list. However, it avoids three unnecessary database calls.
	           */
	          if(Objects.nonNull(project)) {
	            project.getMaterials().addAll(fetchMaterialsForProject(conn, projectId));
	            project.getSteps().addAll(fetchStepsForProject(conn, projectId));
	            project.getCategories().addAll(fetchCategoriesForProject(conn, projectId));
	          }

	          commitTransaction(conn);  
	        
	        
	        return Optional.ofNullable(project);
	      }
	      catch(Exception e) {
	        rollbackTransaction(conn);
	        throw new DbException(e);
	      }
	    }
	    catch(SQLException e) {
	      throw new DbException(e);
	    }
	  }  
	

	private List<Category> fetchCategoriesForProject(Connection conn, Integer projectId) {
	    // @formatter:off
	    String sql = ""
	        + "SELECT c.* FROM " + CATEGORY_TABLE + " c "
	        + "JOIN " + PROJECT_CATEGORY_TABLE + " pc USING (category_id) "
	        + "WHERE project_id = ?";
	    // @formatter:on

	    try(PreparedStatement stmt = conn.prepareStatement(sql)) {
	      setParameter(stmt, 1, projectId, Integer.class);

	      try(ResultSet rs = stmt.executeQuery()) {
	        List<Category> categories = new LinkedList<>();

	        while(rs.next()) {
	          categories.add(extract(rs, Category.class));
	        }

	        return categories;
	      }
	    }
	    catch(SQLException e) {
	      throw new DbException(e);
	    }
	  }

	  /**
	   * This method uses JDBC method calls to retrieve project steps for the given project ID. The
	   * connection is supplied by the caller so that steps can be retrieved on the current transaction.
	   * 
	   * @param conn The caller-supplied connection.
	   * @param projectId The project ID used to retrieve the steps.
	   * @return A list of steps in step order.
	   * @throws SQLException Thrown if the database driver encounters an error.
	   */
	  private List<Step> fetchStepsForProject(Connection conn, Integer projectId) throws SQLException {
	    String sql = "SELECT * FROM " + STEP_TABLE + " WHERE project_id = ?";

	    try(PreparedStatement stmt = conn.prepareStatement(sql)) {
	      setParameter(stmt, 1, projectId, Integer.class);

	      try(ResultSet rs = stmt.executeQuery()) {
	        List<Step> steps = new LinkedList<>();

	        while(rs.next()) {
	          steps.add(extract(rs, Step.class));
	        }

	        return steps;
	      }
	    }
	  }

	  /**
	   * This method uses JDBC method calls to retrieve project materials for the given project ID. The
	   * connection is supplied by the caller so that project materials can be retrieved on the current
	   * transaction.
	   * 
	   * @param conn The caller-supplied connection.
	   * @param projectId The project ID used to retrieve the materials.
	   * @return A list of materials.
	   * @throws SQLException Thrown if the database driver encounters an error.
	   */
	  private List<Material> fetchMaterialsForProject(Connection conn, Integer projectId)
	      throws SQLException {
	    String sql = "SELECT * FROM " + MATERIAL_TABLE + " WHERE project_id = ?";

	    try(PreparedStatement stmt = conn.prepareStatement(sql)) {
	      setParameter(stmt, 1, projectId, Integer.class);

	      try(ResultSet rs = stmt.executeQuery()) {
	        List<Material> materials = new LinkedList<>();

	        while(rs.next()) {
	          materials.add(extract(rs, Material.class));
	        }

	        return materials;
	      }
	    }
	  }

	  public boolean modifyProjectDetails(Project project) {
		    // @formatter:off
		    String sql = ""
		        + "UPDATE " + PROJECT_TABLE + " SET "
		        + "project_name = ?, "
		        + "estimated_hours = ?, "
		        + "actual_hours = ?, "
		        + "difficulty = ?, "
		        + "notes = ? "
		        + "WHERE project_id = ?";
		    // @formatter:on

		    try(Connection conn = DbConnection.getConnection()) {
		      startTransaction(conn);

		      try(PreparedStatement stmt = conn.prepareStatement(sql)) {
		        setParameter(stmt, 1, project.getProjectName(), String.class);
		        setParameter(stmt, 2, project.getEstimatedHours(), BigDecimal.class);
		        setParameter(stmt, 3, project.getActualHours(), BigDecimal.class);
		        setParameter(stmt, 4, project.getDifficulty(), Integer.class);
		        setParameter(stmt, 5, project.getNotes(), String.class);
		        setParameter(stmt, 6, project.getProjectId(), Integer.class);

		        boolean modified = stmt.executeUpdate() == 1;
		        commitTransaction(conn);

		        return modified;
		      }
		      catch(Exception e) {
		        rollbackTransaction(conn);
		        throw new DbException(e);
		      }
		    }
		    catch(SQLException e) {
		      throw new DbException(e);
		    }
	  }

	  public boolean deleteProject(Integer projectId) {
    String sql = "DELETE FROM " + PROJECT_TABLE + " WHERE project_id = ?";

    try(Connection conn = DbConnection.getConnection()) {
      startTransaction(conn);

      try(PreparedStatement stmt = conn.prepareStatement(sql)) {
        setParameter(stmt, 1, projectId, Integer.class);

        /*
         * If the project ID is correct, the number of rows modified will be 1. This is the value
         * returned by executeUpdate(). The value will be 1 even if child rows are deleted because
         * ON DELETE CASCADE is specified.
         */
        boolean deleted = stmt.executeUpdate() == 1;

        commitTransaction(conn);
        return deleted;
      }
      catch(Exception e) {
        rollbackTransaction(conn);
        throw new DbException(e);
      }
    }
    catch(SQLException e) {
      throw new DbException(e);
    }
  }

}