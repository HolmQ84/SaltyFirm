package com.saltyfirm.saltyfirm.Repositories;

import com.saltyfirm.saltyfirm.Models.Firm;
import com.saltyfirm.saltyfirm.Models.Review;
import com.saltyfirm.saltyfirm.Repositories.DatabaseHelper.ProjectVariables;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReviewRepositoryImpl implements ReviewRepository {

    @Override
    public Review findReviewById(int reviewId) {

        try {
            Connection connection = DriverManager.getConnection(ProjectVariables.getUrl(), ProjectVariables.getUsername(), ProjectVariables.getPassword());
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT firm_name, department_name, "+
                            "review_id, post, salary, position, pension_scheme, benefits, management, work_environment, "+
                            "flexibility, employment_time FROM saltyfirm.review, saltyfirm.department, saltyfirm.firm" +
                            "WHERE review_id = ? AND department_fk_id = department_id AND firm_fk_id = firm_id;");

            preparedStatement.setInt(1, reviewId);
            ResultSet resultSet = preparedStatement.executeQuery();

            if(resultSet.next()) {
                Review review = new Review();

                review.setReviewId(resultSet.getInt("review_id"));
                review.setDepartmentName(resultSet.getString("department_name"));
                review.setFirmName(resultSet.getString("firm_name"));
                review.setPost(resultSet.getString("post"));
                review.setSalary(resultSet.getInt("salary"));
                review.setPosition(resultSet.getString("position"));
                review.setPensionScheme(resultSet.getInt("pension_scheme"));
                review.setBenefits(resultSet.getInt("benefits"));
                review.setManagement(resultSet.getInt("management"));
                review.setWorkEnvironment(resultSet.getInt("work_environment"));
                review.setFlexibility(resultSet.getInt("flexibility"));
                review.setEmploymentTime(resultSet.getInt("employment_time"));
                return review;
            }
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int createReview(Review review, int userId, int departmentId) {

        try {
            Connection connection = DriverManager.getConnection(ProjectVariables.getUrl(), ProjectVariables.getUsername(), ProjectVariables.getPassword());
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO saltyfirm.review (post, salary, position, pension_scheme, benefits," +
                    "management, work_environment, flexibility, employment_time, user_fk_id, department_fk_id) " +
                    "VALUES (?,?,?,?,?,?,?,?,?,?,?)");

            preparedStatement.setString(1, review.getPost());
            preparedStatement.setInt(2, review.getSalary());
            preparedStatement.setString(3, review.getPosition());
            preparedStatement.setDouble(4, review.getPensionScheme());
            preparedStatement.setDouble(5, review.getBenefits());
            preparedStatement.setDouble(6, review.getManagement());
            preparedStatement.setDouble(7, review.getWorkEnvironment());
            preparedStatement.setDouble(8, review.getFlexibility());
            preparedStatement.setInt(9, review.getEmploymentTime());
            preparedStatement.setInt(10, userId);
            preparedStatement.setInt(11, departmentId);

            preparedStatement.executeUpdate();

            preparedStatement = connection.prepareStatement("UPDATE saltyfirm.department \n" +
                                                                "SET department_score = \n" +
                                                                "  (SELECT\n" +
                                                                "    (SELECT SUM(pension_scheme + benefits + management + work_environment + flexibility) / 5 AS total_score) /\n" +
                                                                "    (SELECT COUNT(benefits)) AS total_total_score\n" +
                                                                "    FROM review\n" +
                                                                "    WHERE department_fk_id = department_id) \n" +
                                                                "WHERE department_id = ?;");
            preparedStatement.setInt(1, departmentId);
            preparedStatement.executeUpdate();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    public int editReview(Review review, int departmentId) {

        try {
            Connection connection = DriverManager.getConnection(ProjectVariables.getUrl(), ProjectVariables.getUsername(), ProjectVariables.getPassword());
            PreparedStatement preparedStatement = connection.prepareStatement("UPDATE saltyfirm.review SET post, salary, position, pension_scheme, benefits, " +
                    "management, work_environment, flexibility, employment_time VALUE (?,?,?,?,?,?,?,?,?)");

            preparedStatement.setString(1, review.getPost());
            preparedStatement.setInt(2, review.getSalary());
            preparedStatement.setString(3, review.getPosition());
            preparedStatement.setDouble(4, review.getPensionScheme());
            preparedStatement.setDouble(5, review.getBenefits());
            preparedStatement.setDouble(6, review.getManagement());
            preparedStatement.setDouble(7, review.getWorkEnvironment());
            preparedStatement.setDouble(8, review.getFlexibility());
            preparedStatement.setInt(9, review.getEmploymentTime());

            preparedStatement.executeUpdate();

            preparedStatement = connection.prepareStatement("UPDATE saltyfirm.department \n" +
                                                                "SET department_score = \n" +
                                                                "  (SELECT\n" +
                                                                "    (SELECT SUM(pension_scheme + benefits + management + work_environment + flexibility) / 5 AS total_score) /\n" +
                                                                "    (SELECT COUNT(benefits)) AS total_total_score\n" +
                                                                "    FROM review\n" +
                                                                "    WHERE department_fk_id = department_id) \n" +
                                                                "WHERE department_id = ?;");

            preparedStatement.setInt(1, departmentId);
            preparedStatement.executeUpdate();

            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }


        return 0;
    }

    @Override
    public int deleteReview(int reviewId) {
        int departmentId;
        try {
            Connection connection = DriverManager.getConnection(ProjectVariables.getUrl(), ProjectVariables.getUsername(), ProjectVariables.getPassword());
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT department_id FROM saltyfirm.department, saltyfirm.review WHERE department_id = department_fk_id AND review_id = ?");
            preparedStatement.setInt(1, reviewId);
            ResultSet resultSet = preparedStatement.executeQuery();
            departmentId = resultSet.getInt(1);

            preparedStatement = connection.prepareStatement("DELETE FROM saltyfirm.review WHERE review_id = ?");
            preparedStatement.setInt(1, reviewId);
            preparedStatement.executeUpdate();

            preparedStatement = connection.prepareStatement("UPDATE saltyfirm.department \n" +
                                                                "SET department_score = \n" +
                                                                "  (SELECT\n" +
                                                                "    (SELECT SUM(pension_scheme + benefits + management + work_environment + flexibility) / 5 AS total_score) /\n" +
                                                                "    (SELECT COUNT(review_id)) AS total_total_score\n" +
                                                                "    FROM review\n" +
                                                                "    WHERE department_fk_id = department_id) \n" +
                                                                "WHERE department_id = ?;");

            preparedStatement.setInt(1, departmentId);
            preparedStatement.executeUpdate();
            connection.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }


        return 0;
    }

    // Denne metode ligger også i DepartmentRepository
    public List<Review> getAllReviews(int departmentId) {
        List<Review> reviews = new ArrayList<>();
        try {
            Connection connection = DriverManager.getConnection(ProjectVariables.getUrl(), ProjectVariables.getUsername(), ProjectVariables.getPassword());
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM saltyfirm.review WHERE department_fk_id = ?");
            preparedStatement.setInt(1, departmentId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Review review = new Review();
                review.setReviewId(resultSet.getInt("review_id"));
                review.setPost(resultSet.getString("post"));
                review.setSalary(resultSet.getInt("salary"));
                review.setPosition(resultSet.getString("position"));
                review.setPensionScheme(resultSet.getInt("pension_scheme"));
                review.setBenefits(resultSet.getInt("benefits"));
                review.setManagement(resultSet.getInt("management"));
                review.setWorkEnvironment(resultSet.getInt("work_environment"));
                review.setFlexibility(resultSet.getInt("flexibility"));
                review.setEmploymentTime(resultSet.getInt("employment_time"));
                reviews.add(review);
            }
            connection.close();
            return reviews;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public List<Review> fetchUserReview(int userId) {
        List<Review> userReviews = new ArrayList<>();

        try {
            Connection connection = DriverManager.getConnection(ProjectVariables.getUrl(), ProjectVariables.getUsername(), ProjectVariables.getPassword());
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT firm_name, department_name, review_id, post, salary, position, pension_scheme, benefits, management, work_environment, flexibility, employment_time\n" +
                    "\t\tFROM saltyfirm.review, saltyfirm.department, saltyfirm.firm\n" +
                    "\t\tWHERE user_fk_id = ? AND department_fk_id = department_id AND firm_fk_id = firm_id;");
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                Review review = new Review();
                review.setFirmName(resultSet.getString("firm_name"));
                review.setDepartmentName(resultSet.getString("department_name"));
                review.setReviewId(resultSet.getInt("review_id"));
                review.setBenefits(resultSet.getInt("benefits"));
                review.setSalary(resultSet.getInt("salary"));
                review.setEmploymentTime(resultSet.getInt("employment_time"));
                review.setFlexibility(resultSet.getInt("flexibility"));
                review.setManagement(resultSet.getInt("management"));
                review.setPensionScheme(resultSet.getInt("pension_scheme"));
                review.setWorkEnvironment(resultSet.getInt("work_environment"));
                review.setPosition(resultSet.getString("position"));
                review.setPost(resultSet.getString("post"));
                userReviews.add(review);
            }
            connection.close();

            return userReviews;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


}
