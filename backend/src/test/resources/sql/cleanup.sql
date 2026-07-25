-- Cleanup script for integration tests
-- Executed before each test method within the test transaction
-- @Transactional on test class ensures automatic rollback after each test
-- This script only targets specific test user emails created by integration tests

DELETE FROM mst_users WHERE email IN ('newuser@example.com', 'duplicate@example.com');
