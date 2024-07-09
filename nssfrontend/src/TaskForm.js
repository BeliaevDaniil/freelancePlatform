import React, { useState } from 'react';
import axios from 'axios';
import './TaskForm.css'; // Убедитесь, что стили подключены
import Cookies from 'js-cookie';

function TaskForm() {
    const [title, setTitle] = useState('');
    const [problem, setProblem] = useState('');
    const [deadline, setDeadline] = useState('');
    const [payment, setPayment] = useState('');
    const [selectedType, setSelectedType] = useState(''); // Здесь будет храниться выбранный тип задачи
    const [showDropdown, setShowDropdown] = useState(false);

    const savedUsername = Cookies.get('username');
    const savedEmail = Cookies.get('email');
    const savedAuthToken = Cookies.get('authToken');

    const toggleDropdown = () => setShowDropdown(!showDropdown);

    const taskTypes = [
        "TranslationAndLanguageServices",
        "DataEntryAndVirtualAssistance",
        "ConsultingAndBusinessServices",
        "CreativeAndArtisticServices",
        "GraphicDesignAndMultimedia",
        "EngineeringAndArchitecture",
        "WritingAndContentCreation",
        "ProgrammingAndDevelopment",
        "GamingAndVrArDevelopment",
        "TutoringAndEducation",
        "SalesAndMarketing",
        "DigitalMarketing"
    ];

    const handleSubmit = async (event) => {
        event.preventDefault();
        const taskData = {
            username: savedUsername,
            email: savedEmail,
            title,
            problem,
            deadline: new Date(deadline).toISOString(),
            taskStatus: 'UNASSIGNED',
            payment,
            type: selectedType // Только один выбранный тип задачи
        };

        try {
            // Отправка данных на сервер с использованием axios
            const response = await axios.post('http://localhost:8080/rest/tasks', taskData, {
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': savedAuthToken // Подставляем реальный токен из кук
                }
            });

            if (response.status === 200 || response.status === 201) {
                alert('Task created successfully!');
                setTitle('');
                setProblem('');
                setDeadline('');
                setPayment('');
                setSelectedType(''); // Сброс выбранного типа
            } else {
                alert('Failed to create task');
                console.log('Failed to create task with status:', response.status);
            }
        } catch (error) {
            if (error.response) {
                console.error('Response error:', error.response);
            } else {
                console.error('Error:', error.message);
            }
            alert('Error creating task. See console for details.');
        }
    };

    return (
        <div className="form-container">
            <form onSubmit={handleSubmit}>
                <label>
                    Title:
                    <input type="text" value={title} onChange={e => setTitle(e.target.value)} />
                </label>
                <label>
                    Problem Description:
                    <textarea value={problem} onChange={e => setProblem(e.target.value)} />
                </label>
                <label>
                    Deadline:
                    <input type="datetime-local" value={deadline} onChange={e => setDeadline(e.target.value)} />
                </label>
                <label>
                    Payment:
                    <input type="number" value={payment} onChange={e => setPayment(e.target.value)} />
                </label>
                <div className="radio-group">
                    {taskTypes.map((type, index) => (
                        <label key={index} className="radio-label">
                            <input
                                type="radio"
                                value={type}
                                checked={selectedType === type}
                                onChange={() => setSelectedType(type)}
                            />
                            {type}
                        </label>
                    ))}
                </div>
                <button type="submit">Save Task</button>
            </form>
        </div>
    );
}

export default TaskForm;
