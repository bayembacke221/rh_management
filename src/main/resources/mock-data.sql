-- Jeu de donnees mock RH Management (PostgreSQL)
-- Couvre toutes les entites JPA du backend.

BEGIN;

-- Nettoyage pour pouvoir rejouer le script facilement
TRUNCATE TABLE
    notification_readers,
    notification_recipients,
    users_roles,
    roles_permissions,
    token,
    notifications,
    leaves,
    leave_balances,
    leave_policies,
    documents,
    contracts,
    employees,
    positions,
    departements,
    users,
    roles,
    permissions
RESTART IDENTITY CASCADE;

-- 1) Permissions
INSERT INTO permissions (id, name, description, category, created_date, created_by)
VALUES
    (1, 'READ_EMPLOYEE', 'Consulter les employes', 'EMPLOYEE', NOW(), 'seed-script'),
    (2, 'WRITE_CONTRACT', 'Creer et modifier les contrats', 'ADMIN', NOW(), 'seed-script'),
    (3, 'APPROVE_LEAVE', 'Valider les demandes de conge', 'LEAVE', NOW(), 'seed-script');

-- 2) Roles
INSERT INTO roles (id, name, description, created_date)
VALUES
    (1, 'ROLE_ADMIN', 'Administrateur de la plateforme RH', NOW()),
    (2, 'ROLE_RH_MANAGER', 'Responsable RH', NOW()),
    (3, 'ROLE_EMPLOYEE', 'Employe standard', NOW());

-- 3) Association roles <-> permissions (table implicite JPA)
INSERT INTO roles_permissions
VALUES
    ((SELECT id FROM roles WHERE name = 'ROLE_ADMIN'), (SELECT id FROM permissions WHERE name = 'READ_EMPLOYEE')),
    ((SELECT id FROM roles WHERE name = 'ROLE_ADMIN'), (SELECT id FROM permissions WHERE name = 'WRITE_CONTRACT')),
    ((SELECT id FROM roles WHERE name = 'ROLE_ADMIN'), (SELECT id FROM permissions WHERE name = 'APPROVE_LEAVE')),
    ((SELECT id FROM roles WHERE name = 'ROLE_RH_MANAGER'), (SELECT id FROM permissions WHERE name = 'READ_EMPLOYEE')),
    ((SELECT id FROM roles WHERE name = 'ROLE_RH_MANAGER'), (SELECT id FROM permissions WHERE name = 'WRITE_CONTRACT')),
    ((SELECT id FROM roles WHERE name = 'ROLE_RH_MANAGER'), (SELECT id FROM permissions WHERE name = 'APPROVE_LEAVE')),
    ((SELECT id FROM roles WHERE name = 'ROLE_EMPLOYEE'), (SELECT id FROM permissions WHERE name = 'READ_EMPLOYEE'));

-- 4) Utilisateurs
-- Mot de passe bcrypt ci-dessous = "password"
INSERT INTO users (id, username, password, email, last_login, account_locked, enabled, created_date, created_by)
VALUES
    (1, 'admin', '$2a$10$7EqJtq98hPqEX7fNZaFWoO5kVwq0n6zkRgZNpmjQe7YQDdyCjTiM.', 'admin@rh.local', NOW() - INTERVAL '1 day', FALSE, TRUE, NOW(), 'seed-script'),
    (2, 'rhmanager', '$2a$10$7EqJtq98hPqEX7fNZaFWoO5kVwq0n6zkRgZNpmjQe7YQDdyCjTiM.', 'rh.manager@rh.local', NOW() - INTERVAL '2 days', FALSE, TRUE, NOW(), 'seed-script'),
    (3, 'a.diop', '$2a$10$7EqJtq98hPqEX7fNZaFWoO5kVwq0n6zkRgZNpmjQe7YQDdyCjTiM.', 'aminata.diop@rh.local', NOW() - INTERVAL '3 days', FALSE, TRUE, NOW(), 'seed-script'),
    (4, 'm.ndiaye', '$2a$10$7EqJtq98hPqEX7fNZaFWoO5kVwq0n6zkRgZNpmjQe7YQDdyCjTiM.', 'mamadou.ndiaye@rh.local', NOW() - INTERVAL '5 days', FALSE, TRUE, NOW(), 'seed-script'),
    (5, 's.fall', '$2a$10$7EqJtq98hPqEX7fNZaFWoO5kVwq0n6zkRgZNpmjQe7YQDdyCjTiM.', 'sokhna.fall@rh.local', NOW() - INTERVAL '6 days', FALSE, TRUE, NOW(), 'seed-script'),
    (6, 'f.sy', '$2a$10$7EqJtq98hPqEX7fNZaFWoO5kVwq0n6zkRgZNpmjQe7YQDdyCjTiM.', 'fatou.sy@rh.local', NOW() - INTERVAL '7 days', FALSE, TRUE, NOW(), 'seed-script');

-- 5) Association users <-> roles (table implicite JPA)
INSERT INTO users_roles
VALUES
    ((SELECT id FROM users WHERE email = 'admin@rh.local'), (SELECT id FROM roles WHERE name = 'ROLE_ADMIN')),
    ((SELECT id FROM users WHERE email = 'rh.manager@rh.local'), (SELECT id FROM roles WHERE name = 'ROLE_RH_MANAGER')),
    ((SELECT id FROM users WHERE email = 'aminata.diop@rh.local'), (SELECT id FROM roles WHERE name = 'ROLE_EMPLOYEE')),
    ((SELECT id FROM users WHERE email = 'mamadou.ndiaye@rh.local'), (SELECT id FROM roles WHERE name = 'ROLE_EMPLOYEE')),
    ((SELECT id FROM users WHERE email = 'sokhna.fall@rh.local'), (SELECT id FROM roles WHERE name = 'ROLE_EMPLOYEE')),
    ((SELECT id FROM users WHERE email = 'fatou.sy@rh.local'), (SELECT id FROM roles WHERE name = 'ROLE_EMPLOYEE'));

-- 6) Departements (manager renseigne apres creation des employes)
INSERT INTO departements (id, name, description, code, manager_id, parent_departement_id, active, created_date, created_by)
VALUES
    (1, 'Direction Generale', 'Pilotage global de l''entreprise', 'DIR', NULL, NULL, TRUE, NOW(), 'seed-script'),
    (2, 'Ressources Humaines', 'Gestion RH et paie', 'RH', NULL, 1, TRUE, NOW(), 'seed-script'),
    (3, 'Informatique', 'Developpement et operations', 'IT', NULL, 1, TRUE, NOW(), 'seed-script'),
    (4, 'Finance', 'Comptabilite et controle de gestion', 'FIN', NULL, 1, TRUE, NOW(), 'seed-script');

-- 7) Positions
INSERT INTO positions (
    id, title, description, grade, min_salary, max_salary, departement_id, responsibilities, requirements, active,
    created_date, created_by
)
VALUES
    (1, 'Directeur RH', 'Supervise la strategie RH', 'G10', 1800000.00, 3000000.00,
     (SELECT id FROM departements WHERE code = 'RH'),
     'Pilotage RH, recrutement, conformite', '10+ ans d''experience RH', TRUE, NOW(), 'seed-script'),
    (2, 'Responsable Recrutement', 'Gere le cycle de recrutement', 'G8', 900000.00, 1500000.00,
     (SELECT id FROM departements WHERE code = 'RH'),
     'Sourcing, entretiens, integration', '5+ ans en recrutement', TRUE, NOW(), 'seed-script'),
    (3, 'Tech Lead', 'Encadre l''equipe de developpement', 'G9', 1200000.00, 2200000.00,
     (SELECT id FROM departements WHERE code = 'IT'),
     'Architecture, mentoring, delivery', '7+ ans en developpement', TRUE, NOW(), 'seed-script'),
    (4, 'Developpeur Backend', 'Developpe les APIs metier', 'G6', 600000.00, 1200000.00,
     (SELECT id FROM departements WHERE code = 'IT'),
     'Conception API, tests, integration', '3+ ans Java/Spring', TRUE, NOW(), 'seed-script'),
    (5, 'Comptable Senior', 'Gere les clotures et reportings', 'G7', 800000.00, 1400000.00,
     (SELECT id FROM departements WHERE code = 'FIN'),
     'Comptabilite generale, reportings', '5+ ans en comptabilite', TRUE, NOW(), 'seed-script');

-- 8) Employes
INSERT INTO employees (
    id, first_name, last_name, phone, birth_date, hire_date, end_date, gender, address, city,
    social_security_number, emergency_contact_name, emergency_contact_phone,
    bank_account_info, cv, status, departement_id, position_id, manager_id, user_id,
    created_date, created_by
)
VALUES
    (1, 'Aissatou', 'Ndiaye', '+221770000001', DATE '1985-02-12', DATE '2018-01-15', NULL, 'FEMALE',
     'Almadies, Villa 24', 'Dakar', 'SN-SS-0001', 'Moussa Ndiaye', '+221770010001',
     'SN00-0001-0001', '/uploads/users/1/cv-aissatou.pdf', 'ACTIVE',
     (SELECT id FROM departements WHERE code = 'RH'),
     (SELECT id FROM positions WHERE title = 'Directeur RH'),
     NULL,
     (SELECT id FROM users WHERE email = 'rh.manager@rh.local'),
     NOW(), 'seed-script'),

    (2, 'Aminata', 'Diop', '+221770000002', DATE '1992-06-03', DATE '2021-03-01', NULL, 'FEMALE',
     'Mermoz, Rue 8', 'Dakar', 'SN-SS-0002', 'Pape Diop', '+221770010002',
     'SN00-0001-0002', '/uploads/users/2/cv-aminata.pdf', 'ACTIVE',
     (SELECT id FROM departements WHERE code = 'IT'),
     (SELECT id FROM positions WHERE title = 'Developpeur Backend'),
     NULL,
     (SELECT id FROM users WHERE email = 'aminata.diop@rh.local'),
     NOW(), 'seed-script'),

    (3, 'Mamadou', 'Ndiaye', '+221770000003', DATE '1988-11-20', DATE '2019-07-10', NULL, 'MALE',
     'Sicap Liberte 6', 'Dakar', 'SN-SS-0003', 'Seynabou Ndiaye', '+221770010003',
     'SN00-0001-0003', '/uploads/users/3/cv-mamadou.pdf', 'ACTIVE',
     (SELECT id FROM departements WHERE code = 'IT'),
     (SELECT id FROM positions WHERE title = 'Tech Lead'),
     NULL,
     (SELECT id FROM users WHERE email = 'mamadou.ndiaye@rh.local'),
     NOW(), 'seed-script'),

    (4, 'Sokhna', 'Fall', '+221770000004', DATE '1995-09-30', DATE '2022-01-12', NULL, 'FEMALE',
     'Yoff Tonghor', 'Dakar', 'SN-SS-0004', 'Awa Fall', '+221770010004',
     'SN00-0001-0004', '/uploads/users/4/cv-sokhna.pdf', 'ON_LEAVE',
     (SELECT id FROM departements WHERE code = 'RH'),
     (SELECT id FROM positions WHERE title = 'Responsable Recrutement'),
     NULL,
     (SELECT id FROM users WHERE email = 'sokhna.fall@rh.local'),
     NOW(), 'seed-script'),

    (5, 'Fatou', 'Sy', '+221770000005', DATE '1990-01-17', DATE '2020-10-05', NULL, 'FEMALE',
     'Parcelles Assainies U13', 'Dakar', 'SN-SS-0005', 'Ibrahima Sy', '+221770010005',
     'SN00-0001-0005', '/uploads/users/5/cv-fatou.pdf', 'ACTIVE',
     (SELECT id FROM departements WHERE code = 'FIN'),
     (SELECT id FROM positions WHERE title = 'Comptable Senior'),
     NULL,
     (SELECT id FROM users WHERE email = 'fatou.sy@rh.local'),
     NOW(), 'seed-script'),

    (6, 'Super', 'Admin', '+221770000000', DATE '1980-04-10', DATE '2015-02-01', NULL, 'MALE',
     'Point E', 'Dakar', 'SN-SS-0000', 'Service securite', '+221770010000',
     'SN00-0001-0000', '/uploads/users/0/cv-admin.pdf', 'ACTIVE',
     (SELECT id FROM departements WHERE code = 'DIR'),
     (SELECT id FROM positions WHERE title = 'Tech Lead'),
     NULL,
     (SELECT id FROM users WHERE email = 'admin@rh.local'),
     NOW(), 'seed-script');

-- Mise a jour des managers des employes
UPDATE employees e
SET manager_id = m.id
FROM employees m
WHERE e.user_id = (SELECT id FROM users WHERE email = 'aminata.diop@rh.local')
  AND m.user_id = (SELECT id FROM users WHERE email = 'mamadou.ndiaye@rh.local');

UPDATE employees e
SET manager_id = m.id
FROM employees m
WHERE e.user_id = (SELECT id FROM users WHERE email = 'sokhna.fall@rh.local')
  AND m.user_id = (SELECT id FROM users WHERE email = 'rh.manager@rh.local');

UPDATE employees e
SET manager_id = m.id
FROM employees m
WHERE e.user_id = (SELECT id FROM users WHERE email = 'fatou.sy@rh.local')
  AND m.user_id = (SELECT id FROM users WHERE email = 'admin@rh.local');

-- Mise a jour des managers de departements
UPDATE departements d
SET manager_id = e.id
FROM employees e
WHERE d.code = 'RH'
  AND e.user_id = (SELECT id FROM users WHERE email = 'rh.manager@rh.local');

UPDATE departements d
SET manager_id = e.id
FROM employees e
WHERE d.code = 'IT'
  AND e.user_id = (SELECT id FROM users WHERE email = 'mamadou.ndiaye@rh.local');

UPDATE departements d
SET manager_id = e.id
FROM employees e
WHERE d.code = 'FIN'
  AND e.user_id = (SELECT id FROM users WHERE email = 'fatou.sy@rh.local');

UPDATE departements d
SET manager_id = e.id
FROM employees e
WHERE d.code = 'DIR'
  AND e.user_id = (SELECT id FROM users WHERE email = 'admin@rh.local');

-- 9) Contrats
INSERT INTO contracts (
    id, employee_id, start_date, end_date, type, salary, work_hours_per_week, status, termination_reason,
    created_date, created_by
)
VALUES
    (1, (SELECT e.id FROM employees e JOIN users u ON e.user_id = u.id WHERE u.email = 'rh.manager@rh.local'),
     DATE '2018-01-15', NULL, 'CDI', 2200000.00, 40, 'ACTIVE', NULL, NOW(), 'seed-script'),
    (2, (SELECT e.id FROM employees e JOIN users u ON e.user_id = u.id WHERE u.email = 'aminata.diop@rh.local'),
     DATE '2024-01-01', NULL, 'CDI', 950000.00, 40, 'ACTIVE', NULL, NOW(), 'seed-script'),
    (3, (SELECT e.id FROM employees e JOIN users u ON e.user_id = u.id WHERE u.email = 'mamadou.ndiaye@rh.local'),
     DATE '2019-07-10', NULL, 'CDI', 1700000.00, 40, 'ACTIVE', NULL, NOW(), 'seed-script'),
    (4, (SELECT e.id FROM employees e JOIN users u ON e.user_id = u.id WHERE u.email = 'sokhna.fall@rh.local'),
     DATE '2022-01-12', DATE '2026-12-31', 'CDD', 1000000.00, 40, 'PENDING', NULL, NOW(), 'seed-script'),
    (5, (SELECT e.id FROM employees e JOIN users u ON e.user_id = u.id WHERE u.email = 'fatou.sy@rh.local'),
     DATE '2020-10-05', NULL, 'CDI', 1150000.00, 40, 'ACTIVE', NULL, NOW(), 'seed-script');

-- 10) Documents
-- Document.type est un enum JPA sans @Enumerated, donc stocke en ORDINAL:
-- CV=0, IDENTITY=1, DIPLOMA=2, CONTRACT=3, PAYSLIP=4, CERTIFICATE=5, ADMINISTRATIVE=6, OTHER=7
INSERT INTO documents (
    id, name, type, path, upload_date, size, content_type, employee_id, contract_id, created_date, created_by
)
VALUES
    (
        1, 'cv-aminata-diop.pdf', 0, '/uploads/users/2/cv-aminata-diop.pdf', NOW() - INTERVAL '10 days', 456789,
        'application/pdf',
        (SELECT e.id FROM employees e JOIN users u ON e.user_id = u.id WHERE u.email = 'aminata.diop@rh.local'),
        (SELECT c.id FROM contracts c JOIN employees e ON c.employee_id = e.id JOIN users u ON e.user_id = u.id WHERE u.email = 'aminata.diop@rh.local'),
        NOW(), 'seed-script'
    ),
    (
        2, 'piece-identite-fatou.jpg', 1, '/uploads/users/5/id-fatou.jpg', NOW() - INTERVAL '8 days', 324567,
        'image/jpeg',
        (SELECT e.id FROM employees e JOIN users u ON e.user_id = u.id WHERE u.email = 'fatou.sy@rh.local'),
        (SELECT c.id FROM contracts c JOIN employees e ON c.employee_id = e.id JOIN users u ON e.user_id = u.id WHERE u.email = 'fatou.sy@rh.local'),
        NOW(), 'seed-script'
    ),
    (
        3, 'contrat-mamadou.pdf', 3, '/uploads/users/3/contrat-mamadou.pdf', NOW() - INTERVAL '3 days', 789123,
        'application/pdf',
        (SELECT e.id FROM employees e JOIN users u ON e.user_id = u.id WHERE u.email = 'mamadou.ndiaye@rh.local'),
        (SELECT c.id FROM contracts c JOIN employees e ON c.employee_id = e.id JOIN users u ON e.user_id = u.id WHERE u.email = 'mamadou.ndiaye@rh.local'),
        NOW(), 'seed-script'
    );

-- 11) Politiques de conges
INSERT INTO leave_policies (
    id, leave_type, year, default_days, max_consecutive_days, min_request_notice_days,
    requires_approval, requires_documentation, is_paid, carry_forward_allowed, max_carry_forward_days,
    description, created_date, created_by
)
VALUES
    (1, 'ANNUAL_LEAVE', 2026, 30.0, 15, 7, TRUE, FALSE, TRUE, TRUE, 10.0,
     'Conge annuel standard', NOW(), 'seed-script'),
    (2, 'SICK_LEAVE', 2026, 10.0, 10, 0, TRUE, TRUE, TRUE, FALSE, 0.0,
     'Conge maladie avec justificatif au-dela de 2 jours', NOW(), 'seed-script'),
    (3, 'MATERNITY_LEAVE', 2026, 98.0, 98, 30, TRUE, TRUE, TRUE, FALSE, 0.0,
     'Conge maternite conforme aux obligations legales', NOW(), 'seed-script'),
    (4, 'PATERNITY_LEAVE', 2026, 10.0, 10, 7, TRUE, FALSE, TRUE, FALSE, 0.0,
     'Conge paternite', NOW(), 'seed-script'),
    (5, 'UNPAID_LEAVE', 2026, 15.0, 15, 15, TRUE, FALSE, FALSE, FALSE, 0.0,
     'Conge sans solde', NOW(), 'seed-script');

-- 12) Soldes de conges
INSERT INTO leave_balances (
    id, employee_id, leave_type, year, initial_balance, used_balance, adjusted_balance, adjustment_reason,
    created_date, created_by
)
VALUES
    (1, (SELECT e.id FROM employees e JOIN users u ON e.user_id = u.id WHERE u.email = 'aminata.diop@rh.local'),
     'ANNUAL_LEAVE', 2026, 30.0, 6.0, 0.0, NULL, NOW(), 'seed-script'),
    (2, (SELECT e.id FROM employees e JOIN users u ON e.user_id = u.id WHERE u.email = 'aminata.diop@rh.local'),
     'SICK_LEAVE', 2026, 10.0, 1.0, 0.0, NULL, NOW(), 'seed-script'),
    (3, (SELECT e.id FROM employees e JOIN users u ON e.user_id = u.id WHERE u.email = 'sokhna.fall@rh.local'),
     'ANNUAL_LEAVE', 2026, 30.0, 12.0, 2.0, 'Regularisation anciennete', NOW(), 'seed-script'),
    (4, (SELECT e.id FROM employees e JOIN users u ON e.user_id = u.id WHERE u.email = 'fatou.sy@rh.local'),
     'ANNUAL_LEAVE', 2026, 30.0, 4.0, 0.0, NULL, NOW(), 'seed-script'),
    (5, (SELECT e.id FROM employees e JOIN users u ON e.user_id = u.id WHERE u.email = 'mamadou.ndiaye@rh.local'),
     'ANNUAL_LEAVE', 2026, 30.0, 8.0, 0.0, NULL, NOW(), 'seed-script');

-- 13) Demandes de conges
INSERT INTO leaves (
    id, employee_id, start_date, end_date, leave_type, status, reason, comments,
    approved_by_id, approval_date, rejection_reason, duration_days, half_day, attachments,
    created_date, created_by
)
VALUES
    (
        1, (SELECT e.id FROM employees e JOIN users u ON e.user_id = u.id WHERE u.email = 'aminata.diop@rh.local'),
        DATE '2026-04-14', DATE '2026-04-18', 'ANNUAL_LEAVE', 'APPROVED',
        'Repos annuel', 'Planifie avec le manager',
        (SELECT e.id FROM employees e JOIN users u ON e.user_id = u.id WHERE u.email = 'mamadou.ndiaye@rh.local'),
        NOW() - INTERVAL '4 days', NULL, 5, FALSE, '/uploads/leaves/aminata-annual-2026.pdf',
        NOW(), 'seed-script'
    ),
    (
        2, (SELECT e.id FROM employees e JOIN users u ON e.user_id = u.id WHERE u.email = 'sokhna.fall@rh.local'),
        DATE '2026-03-10', DATE '2026-03-12', 'SICK_LEAVE', 'APPROVED',
        'Syndrome grippal', 'Certificat medical fourni',
        (SELECT e.id FROM employees e JOIN users u ON e.user_id = u.id WHERE u.email = 'rh.manager@rh.local'),
        NOW() - INTERVAL '15 days', NULL, 3, FALSE, '/uploads/leaves/sokhna-sick-2026.pdf',
        NOW(), 'seed-script'
    ),
    (
        3, (SELECT e.id FROM employees e JOIN users u ON e.user_id = u.id WHERE u.email = 'fatou.sy@rh.local'),
        DATE '2026-05-02', DATE '2026-05-02', 'ANNUAL_LEAVE', 'PENDING',
        'Rendez-vous personnel', 'Demi-journee demandee',
        NULL, NULL, NULL, 0, TRUE, NULL,
        NOW(), 'seed-script'
    ),
    (
        4, (SELECT e.id FROM employees e JOIN users u ON e.user_id = u.id WHERE u.email = 'mamadou.ndiaye@rh.local'),
        DATE '2026-06-01', DATE '2026-06-07', 'TRAINING_LEAVE', 'REJECTED',
        'Formation architecture cloud', 'Budget non valide',
        (SELECT e.id FROM employees e JOIN users u ON e.user_id = u.id WHERE u.email = 'admin@rh.local'),
        NOW() - INTERVAL '2 days', 'Conflit avec priorites du trimestre', 7, FALSE, NULL,
        NOW(), 'seed-script'
    );

-- 14) Notifications
INSERT INTO notifications (
    id, title, content, type, priority, source_type, source_id, action_url, system_wide,
    expiration_date, active, created_date, created_by
)
VALUES
    (
        1, 'Nouvelle demande de conge',
        'Une demande de conge est en attente de validation.',
        'LEAVE_REQUEST', 'MEDIUM', 'LEAVE',
        (SELECT l.id FROM leaves l WHERE l.status = 'PENDING' LIMIT 1),
        '/leaves/pending', FALSE,
        NOW() + INTERVAL '14 days', TRUE, NOW(), 'seed-script'
    ),
    (
        2, 'Integration nouveau collaborateur',
        'Un nouveau collaborateur a rejoint le departement IT.',
        'NEW_EMPLOYEE', 'LOW', 'EMPLOYEE',
        (SELECT e.id FROM employees e JOIN users u ON e.user_id = u.id WHERE u.email = 'aminata.diop@rh.local'),
        '/employees', TRUE,
        NOW() + INTERVAL '30 days', TRUE, NOW(), 'seed-script'
    ),
    (
        3, 'Contrat a surveiller',
        'Un contrat CDD arrive bientot a echeance.',
        'CONTRACT_EXPIRING', 'HIGH', 'CONTRACT',
        (SELECT c.id FROM contracts c JOIN employees e ON c.employee_id = e.id JOIN users u ON e.user_id = u.id WHERE u.email = 'sokhna.fall@rh.local'),
        '/contracts', FALSE,
        NOW() + INTERVAL '10 days', TRUE, NOW(), 'seed-script'
    );

-- 15) Destinataires notifications
INSERT INTO notification_recipients (notification_id, employee_id)
VALUES
    (
        (SELECT id FROM notifications WHERE title = 'Nouvelle demande de conge' LIMIT 1),
        (SELECT e.id FROM employees e JOIN users u ON e.user_id = u.id WHERE u.email = 'rh.manager@rh.local')
    ),
    (
        (SELECT id FROM notifications WHERE title = 'Nouvelle demande de conge' LIMIT 1),
        (SELECT e.id FROM employees e JOIN users u ON e.user_id = u.id WHERE u.email = 'admin@rh.local')
    ),
    (
        (SELECT id FROM notifications WHERE title = 'Contrat a surveiller' LIMIT 1),
        (SELECT e.id FROM employees e JOIN users u ON e.user_id = u.id WHERE u.email = 'rh.manager@rh.local')
    );

-- 16) Notifications deja lues
INSERT INTO notification_readers (notification_id, employee_id)
VALUES
    (
        (SELECT id FROM notifications WHERE title = 'Nouvelle demande de conge' LIMIT 1),
        (SELECT e.id FROM employees e JOIN users u ON e.user_id = u.id WHERE u.email = 'admin@rh.local')
    );

-- 17) Tokens de validation/authentification
INSERT INTO token (id, token, created_at, expires_at, validated_at, user_id)
VALUES
    (1, 'mock-token-admin-001', NOW() - INTERVAL '2 days', NOW() + INTERVAL '5 days', NOW() - INTERVAL '1 day',
     (SELECT id FROM users WHERE email = 'admin@rh.local')),
    (2, 'mock-token-rh-001', NOW() - INTERVAL '1 day', NOW() + INTERVAL '3 days', NULL,
     (SELECT id FROM users WHERE email = 'rh.manager@rh.local'));

COMMIT;

